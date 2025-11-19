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

import com.example.whatsapp.model.Recipient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    private final AIProviderService aiProviderService;
    private final ConversationHistoryService conversationHistoryService;
    private final ConversationSentimentService conversationSentimentService;
    private final RecipientService recipientService;

    // Constructor
    public WhatsAppService(WebClient.Builder webClientBuilder, GroqService groqService,
                          AIProviderService aiProviderService, ConversationHistoryService conversationHistoryService,
                          ConversationSentimentService conversationSentimentService, RecipientService recipientService) {
        this.webClientBuilder = webClientBuilder;
        this.groqService = groqService;
        this.aiProviderService = aiProviderService;
        this.conversationHistoryService = conversationHistoryService;
        this.conversationSentimentService = conversationSentimentService;
        this.recipientService = recipientService;
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

                            // For non-text messages, send a personalized acknowledgment
                            Optional<Recipient> recipient = recipientService.getRecipientByPhoneNumber(senderPhoneNumber);
                            String name = recipient.map(Recipient::getDisplayName).orElse("there");
                            String responseMessage = String.format("Thank you for sharing that with me, %s! While I can't see images or other media yet, I'm here to chat with you. How are you feeling today?", name);
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
                // Send initial greeting - look up recipient for personalized message
                conversationHistoryService.markFirstMessageProcessed(phoneNumber);

                // Look up recipient in JSON config for personalized greeting
                Optional<Recipient> recipientOpt = recipientService.getRecipientByPhoneNumber(phoneNumber);
                String greeting;

                if (recipientOpt.isPresent()) {
                    Recipient recipient = recipientOpt.get();
                    String name = recipient.getDisplayName();
                    String customMessage = recipient.getCustomMessage();

                    if (customMessage != null && !customMessage.isEmpty()) {
                        // Use custom message from config, replacing {name} placeholder
                        greeting = customMessage.replace("{name}", name);
                        log.info("Using custom greeting for {} ({})", name, phoneNumber);
                    } else {
                        // Generate personalized greeting with recipient's name
                        greeting = String.format("Hello %s! 🌺 How are you doing today? I'm here to chat with you and keep you company. Please tell me, how has your day been so far?", name);
                        log.info("Using default personalized greeting for {} ({})", name, phoneNumber);
                    }
                } else {
                    // Fallback to generic greeting for unknown numbers
                    greeting = aiProviderService.generateInitialGreeting(phoneNumber);
                    log.info("Using generic greeting for unknown number: {}", phoneNumber);
                }

                // Add to conversation history
                conversationHistoryService.addUserMessage(phoneNumber, userMessage);
                conversationHistoryService.addAssistantMessage(phoneNumber, greeting);

                return greeting;
            }
            
            // Add user message to conversation history
            conversationHistoryService.addUserMessage(phoneNumber, userMessage);
            
            // Generate AI response with multi-provider support
            String response;
            if (aiEnabled && aiProviderService.hasAvailableProviders()) {
                try {
                    // Get conversation history
                    List<ConversationHistoryService.ChatMessage> conversationHistory = conversationHistoryService.getConversationHistory(phoneNumber);
                    response = aiProviderService.generateResponse(conversationHistory, userMessage);
                    log.debug("Using AI provider service for response generation");
                } catch (Exception aiException) {
                    log.warn("All AI providers failed, using fallback: {}", aiException.getMessage());
                    response = getFallbackResponse(userMessage);
                }
            } else if (aiEnabled && groqService.isConfigured()) {
                // Fallback to Groq service if multi-provider is not available
                try {
                    List<ConversationHistoryService.ChatMessage> conversationHistory = conversationHistoryService.getConversationHistory(phoneNumber);
                    response = groqService.generateCompanionResponse(conversationHistory, userMessage);
                    log.debug("Using Groq fallback service for response generation");
                } catch (Exception groqException) {
                    log.warn("Groq service failed, using static fallback: {}", groqException.getMessage());
                    response = getFallbackResponse(userMessage);
                }
            } else {
                log.debug("AI not enabled or no providers configured, using fallback response");
                response = getFallbackResponse(userMessage);
            }
            
            // Add assistant response to conversation history
            conversationHistoryService.addAssistantMessage(phoneNumber, response);
            
            // Analyze and store sentiment for this conversation
            try {
                conversationSentimentService.analyzeAndStoreSentiment(phoneNumber, userMessage, response);
                log.debug("Sentiment analysis completed for user: {}", phoneNumber);
            } catch (Exception e) {
                log.warn("Failed to analyze sentiment for user {}: {}", phoneNumber, e.getMessage());
            }
            
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