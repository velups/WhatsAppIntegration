package com.example.whatsapp.controller;

import com.example.whatsapp.entity.RecipientEntity;
import com.example.whatsapp.service.RecipientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    /**
     * Get all recipients
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRecipients() {
        List<RecipientEntity> recipients = recipientService.getAllRecipients();

        Map<String, Object> response = new HashMap<>();
        response.put("total", recipients.size());
        response.put("enabled_count", recipientService.getEnabledRecipientsCount());
        response.put("recipients", recipients);

        return ResponseEntity.ok(response);
    }

    /**
     * Get enabled recipients only
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<RecipientEntity>> getEnabledRecipients() {
        return ResponseEntity.ok(recipientService.getEnabledRecipients());
    }

    /**
     * Get a recipient by phone number
     */
    @GetMapping("/{phoneNumber}")
    public ResponseEntity<RecipientEntity> getRecipient(@PathVariable String phoneNumber) {
        return recipientService.getRecipientByPhoneNumber(phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Add a new recipient
     */
    @PostMapping
    public ResponseEntity<?> addRecipient(@RequestBody RecipientEntity recipient) {
        try {
            RecipientEntity saved = recipientService.addRecipient(recipient);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update a recipient
     */
    @PutMapping("/{phoneNumber}")
    public ResponseEntity<?> updateRecipient(
            @PathVariable String phoneNumber,
            @RequestBody RecipientEntity recipient) {
        try {
            RecipientEntity updated = recipientService.updateRecipient(phoneNumber, recipient);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete a recipient
     */
    @DeleteMapping("/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> deleteRecipient(@PathVariable String phoneNumber) {
        Map<String, Object> response = new HashMap<>();

        if (recipientService.removeRecipient(phoneNumber)) {
            response.put("success", true);
            response.put("message", "Recipient deleted successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Recipient not found");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Enable or disable a recipient
     */
    @PatchMapping("/{phoneNumber}/enable")
    public ResponseEntity<Map<String, Object>> toggleRecipient(
            @PathVariable String phoneNumber,
            @RequestParam boolean enabled) {

        Map<String, Object> response = new HashMap<>();

        if (recipientService.getRecipientByPhoneNumber(phoneNumber).isPresent()) {
            recipientService.enableRecipient(phoneNumber, enabled);
            response.put("success", true);
            response.put("enabled", enabled);
            response.put("message", enabled ? "Recipient enabled" : "Recipient disabled");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Recipient not found");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get recipients due for wellness check
     */
    @GetMapping("/due-for-check")
    public ResponseEntity<List<RecipientEntity>> getRecipientsDueForCheck() {
        return ResponseEntity.ok(recipientService.getRecipientsDueForCheck());
    }

    /**
     * Get recipient statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_recipients", recipientService.getTotalRecipientsCount());
        stats.put("enabled_recipients", recipientService.getEnabledRecipientsCount());
        stats.put("due_for_check", recipientService.getRecipientsDueForCheck().size());
        return ResponseEntity.ok(stats);
    }
}
