package com.example.whatsapp.controller;

import com.example.whatsapp.dto.WhatsAppMessageRequest;
import com.example.whatsapp.dto.WhatsAppMessageResponse;
import com.example.whatsapp.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {
    
    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);
    
    private final WhatsAppService whatsAppService;
    
    // Constructor
    public WhatsAppWebhookController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }
    
    /**
     * Webhook verification endpoint for WhatsApp Business API
     * WhatsApp will send a GET request to verify the webhook URL
     */
    @GetMapping("/webhook")
    public ResponseEntity<String> verifyWebhook(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {
        
        log.info("Webhook verification request received - mode: {}, token: {}", mode, verifyToken);
        
        if (whatsAppService.verifyWebhook(mode, verifyToken)) {
            log.info("Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        } else {
            log.error("Webhook verification failed");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Verification failed");
        }
    }
    
    /**
     * Webhook endpoint to receive messages from WhatsApp
     * WhatsApp will send a POST request with message data
     */
    @PostMapping("/webhook")
    public ResponseEntity<WhatsAppMessageResponse> receiveMessage(@RequestBody WhatsAppMessageRequest request) {
        log.info("Received WhatsApp message: {}", request);
        
        try {
            // Process the incoming message and send response
            WhatsAppMessageResponse response = whatsAppService.processMessage(request);
            
            log.info("Message processed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing WhatsApp message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(WhatsAppMessageResponse.error("Failed to process message"));
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("WhatsApp Integration API is running");
    }
}