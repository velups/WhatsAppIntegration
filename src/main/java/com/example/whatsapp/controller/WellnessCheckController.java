package com.example.whatsapp.controller;

import com.example.whatsapp.dto.WellnessCheckRequest;
import com.example.whatsapp.dto.WellnessCheckResponse;
import com.example.whatsapp.model.Recipient;
import com.example.whatsapp.service.RecipientService;
import com.example.whatsapp.service.WellnessCheckService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wellness")
public class WellnessCheckController {
    
    private static final Logger log = LoggerFactory.getLogger(WellnessCheckController.class);
    
    private final WellnessCheckService wellnessCheckService;
    private final RecipientService recipientService;
    
    public WellnessCheckController(WellnessCheckService wellnessCheckService, RecipientService recipientService) {
        this.wellnessCheckService = wellnessCheckService;
        this.recipientService = recipientService;
    }
    
    /**
     * Send a wellness check message to a specific recipient
     */
    @PostMapping("/send")
    public ResponseEntity<WellnessCheckResponse> sendWellnessCheck(@Valid @RequestBody WellnessCheckRequest request) {
        log.info("Sending wellness check to {} ({})", request.getName(), request.getPhoneNumber());
        
        WellnessCheckResponse response = wellnessCheckService.sendWellnessCheck(request);
        
        if ("success".equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Send wellness check to all enabled recipients
     */
    @PostMapping("/send-all")
    public ResponseEntity<Map<String, Object>> sendWellnessCheckToAll() {
        log.info("Triggering wellness check for all enabled recipients");
        
        List<Recipient> enabledRecipients = recipientService.getEnabledRecipients();
        int successCount = 0;
        int failureCount = 0;
        
        for (Recipient recipient : enabledRecipients) {
            try {
                WellnessCheckRequest request = WellnessCheckRequest.builder()
                        .phoneNumber(recipient.getPhoneNumber())
                        .name(recipient.getName())
                        .customMessage(recipient.getCustomMessage())
                        .timeOfDay(recipient.getPreferredTimeOfDay())
                        .build();
                
                WellnessCheckResponse response = wellnessCheckService.sendWellnessCheck(request);
                
                if ("success".equals(response.getStatus())) {
                    successCount++;
                } else {
                    failureCount++;
                }
                
                // Add delay to avoid rate limiting
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Failed to send wellness check to {} ({})", 
                        recipient.getName(), recipient.getPhoneNumber(), e);
                failureCount++;
            }
        }
        
        Map<String, Object> result = Map.of(
                "status", "completed",
                "totalRecipients", enabledRecipients.size(),
                "successCount", successCount,
                "failureCount", failureCount,
                "message", String.format("Wellness checks sent to %d recipients (%d success, %d failed)", 
                        enabledRecipients.size(), successCount, failureCount)
        );
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Get all recipients
     */
    @GetMapping("/recipients")
    public ResponseEntity<List<Recipient>> getAllRecipients() {
        return ResponseEntity.ok(recipientService.getAllRecipients());
    }
    
    /**
     * Get enabled recipients only
     */
    @GetMapping("/recipients/enabled")
    public ResponseEntity<List<Recipient>> getEnabledRecipients() {
        return ResponseEntity.ok(recipientService.getEnabledRecipients());
    }
    
    /**
     * Get recipients due for wellness check
     */
    @GetMapping("/recipients/due")
    public ResponseEntity<List<Recipient>> getRecipientsDue() {
        return ResponseEntity.ok(recipientService.getRecipientsDueForCheck());
    }
    
    /**
     * Add a new recipient
     */
    @PostMapping("/recipients")
    public ResponseEntity<Map<String, String>> addRecipient(@Valid @RequestBody Recipient recipient) {
        log.info("Adding new recipient: {} ({})", recipient.getName(), recipient.getPhoneNumber());
        
        // Set default values
        if (recipient.getPreferredTimeOfDay() == null || recipient.getPreferredTimeOfDay().isEmpty()) {
            recipient.setPreferredTimeOfDay("morning");
        }
        if (recipient.getTimezone() == null || recipient.getTimezone().isEmpty()) {
            recipient.setTimezone("UTC");
        }
        
        recipientService.addRecipient(recipient);
        
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Recipient added successfully",
                "name", recipient.getName(),
                "phoneNumber", recipient.getPhoneNumber()
        ));
    }
    
    /**
     * Enable or disable a recipient
     */
    @PutMapping("/recipients/{phoneNumber}/enable")
    public ResponseEntity<Map<String, String>> toggleRecipient(
            @PathVariable String phoneNumber, 
            @RequestParam boolean enabled) {
        
        recipientService.enableRecipient(phoneNumber, enabled);
        
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", String.format("Recipient %s wellness checks", enabled ? "enabled" : "disabled"),
                "phoneNumber", phoneNumber,
                "enabled", String.valueOf(enabled)
        ));
    }
    
    /**
     * Remove a recipient
     */
    @DeleteMapping("/recipients/{phoneNumber}")
    public ResponseEntity<Map<String, String>> removeRecipient(@PathVariable String phoneNumber) {
        boolean removed = recipientService.removeRecipient(phoneNumber);
        
        if (removed) {
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Recipient removed successfully",
                    "phoneNumber", phoneNumber
            ));
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get wellness check statistics and configuration
     */
    @GetMapping("/stats")
    public ResponseEntity<WellnessCheckService.WellnessCheckStats> getStats() {
        return ResponseEntity.ok(wellnessCheckService.getStats());
    }
    
    /**
     * Health check for wellness service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        int enabledCount = recipientService.getEnabledRecipientsCount();
        
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "Wellness Check Service",
                "enabledRecipients", enabledCount,
                "timestamp", System.currentTimeMillis()
        ));
    }
}