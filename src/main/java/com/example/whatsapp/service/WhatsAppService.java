package com.example.whatsapp.service;

import com.example.whatsapp.dto.WhatsAppMessageRequest;
import com.example.whatsapp.dto.WhatsAppMessageResponse;
import com.example.whatsapp.dto.WhatsAppOutgoingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
public class WhatsAppService {
    
    private static final Logger log = LoggerFactory.getLogger(WhatsAppService.class);
    
    @Value("${whatsapp.webhook.verify-token}")
    private String webhookVerifyToken;
    
    @Value("${whatsapp.api.access-token}")
    private String accessToken;
    
    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;
    
    @Value("${whatsapp.api.base-url:https://graph.facebook.com/v18.0}")
    private String whatsAppApiBaseUrl;
    
    @Value("${whatsapp.ai.enabled:true}")
    private boolean aiEnabled;
    
    private final WebClient.Builder webClientBuilder;
    private final GroqService groqService;
    private final ConversationHistoryService conversationHistoryService;
    
    // Constructor
    public WhatsAppService(WebClient.Builder webClientBuilder, GroqService groqService, ConversationHistoryService conversationHistoryService) {
        this.webClientBuilder = webClientBuilder;
        this.groqService = groqService;
        this.conversationHistoryService = conversationHistoryService;
    }
    
    /**
     * Verify the webhook with WhatsApp
     */
    public boolean verifyWebhook(String mode, String verifyToken) {
        return "subscribe".equals(mode) && webhookVerifyToken.equals(verifyToken);
    }
    
    /**
     * Process incoming WhatsApp message with AI companion response
     */
    public WhatsAppMessageResponse processMessage(WhatsAppMessageRequest request) {
        try {
            // Extract message details from the request
            if (request.getEntry() != null && !request.getEntry().isEmpty()) {
                var entry = request.getEntry().get(0);
                
                if (entry.getChanges() != null && !entry.getChanges().isEmpty()) {
                    var change = entry.getChanges().get(0);
                    var value = change.getValue();
                    
                    if (value != null && value.getMessages() != null && !value.getMessages().isEmpty()) {
                        var message = value.getMessages().get(0);
                        String senderPhoneNumber = message.getFrom();
                        String messageType = message.getType();
                        String messageContent = "";
                        
                        // Extract message content based on type
                        if ("text".equals(messageType) && message.getText() != null) {
                            messageContent = message.getText().getBody();
                            log.info("Received text message from {}: {}", senderPhoneNumber, messageContent);
                            
                            // Generate and send AI companion response
                            String responseMessage = generateCompanionResponse(senderPhoneNumber, messageContent);
                            sendWhatsAppMessage(senderPhoneNumber, responseMessage);
                            
                        } else {
                            log.info("Received {} message from {}", messageType, senderPhoneNumber);
                            messageContent = String.format("Received %s message", messageType);
                            
                            // For non-text messages, send a friendly acknowledgment
                            String responseMessage = "Thank you for sharing that with me, Aunty! While I can't see images or other media yet, I'm here to chat with you. How are you feeling today?";
                            sendWhatsAppMessage(senderPhoneNumber, responseMessage);
                        }
                        
                        // Return success response
                        return WhatsAppMessageResponse.success(
                            "Message processed successfully",
                            WhatsAppMessageResponse.MessageData.builder()
                                .messageId(message.getId())
                                .recipientPhoneNumber(senderPhoneNumber)
                                .messageContent(messageContent)
                                .timestamp(String.valueOf(Instant.now().toEpochMilli()))
                                .build()
                        );
                    }
                }
            }
            
            // If we reach here, the message structure was not as expected
            log.warn("Received message with unexpected structure: {}", request);
            return WhatsAppMessageResponse.success("Message received but not processed", null);
            
        } catch (Exception e) {
            log.error("Error processing WhatsApp message", e);
            throw new RuntimeException("Failed to process WhatsApp message", e);
        }
    }
    
    /**
     * Generate a companion response using AI
     */
    private String generateCompanionResponse(String phoneNumber, String userMessage) {
        try {
            // Check if this is the first message from the user
            boolean isFirstMessage = conversationHistoryService.isFirstMessage(phoneNumber);
            
            if (isFirstMessage) {
                // Send initial greeting
                conversationHistoryService.markFirstMessageProcessed(phoneNumber);
                String greeting = groqService.generateInitialGreeting(phoneNumber);
                
                // Add to conversation history
                conversationHistoryService.addUserMessage(phoneNumber, userMessage);
                conversationHistoryService.addAssistantMessage(phoneNumber, greeting);
                
                return greeting;
            }
            
            // Add user message to conversation history
            conversationHistoryService.addUserMessage(phoneNumber, userMessage);
            
            // Generate AI response with fast fallback
            String response;
            if (aiEnabled && groqService.isConfigured()) {
                try {
                    // Get conversation history
                    List<ConversationHistoryService.ChatMessage> conversationHistory = conversationHistoryService.getConversationHistory(phoneNumber);
                    response = groqService.generateCompanionResponse(conversationHistory, userMessage);
                } catch (Exception groqException) {
                    log.warn("Groq API call failed, using fallback: {}", groqException.getMessage());
                    response = getFallbackResponse(userMessage);
                }
            } else {
                log.debug("AI not enabled or configured, using fallback response");
                response = getFallbackResponse(userMessage);
            }
            
            // Add assistant response to conversation history
            conversationHistoryService.addAssistantMessage(phoneNumber, response);
            
            return response;
            
        } catch (Exception e) {
            log.error("Error generating companion response", e);
            // Return a friendly fallback message
            return "Hello Aunty! I'm so glad to hear from you. Please tell me more about your day - I'm here to listen and chat with you!";
        }
    }
    
    private String getFallbackResponse(String userMessage) {
        // Simple keyword-based responses for common messages
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello Aunty! 🌺 How are you doing today? I'm so happy to hear from you!";
        } else if (lowerMessage.contains("good") || lowerMessage.contains("fine") || lowerMessage.contains("ok")) {
            return "That's wonderful to hear, Aunty! I'm so glad you're doing well. Tell me more about your day!";
        } else if (lowerMessage.contains("tired") || lowerMessage.contains("sad") || lowerMessage.contains("not good")) {
            return "Oh dear, I'm sorry to hear that, Aunty. I'm here for you. Would you like to tell me what's on your mind?";
        } else if (lowerMessage.contains("thank")) {
            return "You're so welcome, Aunty! It's my pleasure to chat with you. How else can I brighten your day?";
        } else {
            return "Thank you for sharing that with me, Aunty! I'm here to listen and chat with you. How are you feeling today?";
        }
    }
    
    /**
     * Send a message to WhatsApp user
     */
    private void sendWhatsAppMessage(String recipientPhoneNumber, String messageText) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(whatsAppApiBaseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            WhatsAppOutgoingMessage outgoingMessage = WhatsAppOutgoingMessage.createTextMessage(
                    recipientPhoneNumber, 
                    messageText
            );
            
            String response = webClient
                    .post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(Mono.just(outgoingMessage), WhatsAppOutgoingMessage.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.info("Successfully sent WhatsApp message to {}. Response: {}", recipientPhoneNumber, response);
            
        } catch (Exception e) {
            log.error("Error sending WhatsApp message to {}", recipientPhoneNumber, e);
            // Don't throw exception to avoid breaking the webhook response
        }
    }
}