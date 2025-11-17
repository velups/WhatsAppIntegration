package com.example.whatsapp.service;

import com.example.whatsapp.dto.WhatsAppMessageRequest;
import com.example.whatsapp.dto.WhatsAppMessageResponse;
import com.example.whatsapp.dto.WhatsAppOutgoingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppService {
    
    @Value("${whatsapp.webhook.verify-token}")
    private String webhookVerifyToken;
    
    @Value("${whatsapp.api.access-token}")
    private String accessToken;
    
    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;
    
    @Value("${whatsapp.api.base-url:https://graph.facebook.com/v18.0}")
    private String whatsAppApiBaseUrl;
    
    @Value("${whatsapp.response.static-message:Thank you for your message! This is an automated response. We'll get back to you soon.}")
    private String staticResponseMessage;
    
    private final WebClient.Builder webClientBuilder;
    
    /**
     * Verify the webhook with WhatsApp
     */
    public boolean verifyWebhook(String mode, String verifyToken) {
        return "subscribe".equals(mode) && webhookVerifyToken.equals(verifyToken);
    }
    
    /**
     * Process incoming WhatsApp message and send static response
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
                        } else {
                            log.info("Received {} message from {}", messageType, senderPhoneNumber);
                            messageContent = String.format("Received %s message", messageType);
                        }
                        
                        // Send static response back
                        sendWhatsAppMessage(senderPhoneNumber, staticResponseMessage);
                        
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