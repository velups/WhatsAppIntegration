package com.example.whatsapp.controller;

import com.example.whatsapp.service.ConversationHistoryService;
import com.example.whatsapp.service.GroqService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    private final GroqService groqService;
    private final ConversationHistoryService conversationHistoryService;
    
    public TestController(GroqService groqService, ConversationHistoryService conversationHistoryService) {
        this.groqService = groqService;
        this.conversationHistoryService = conversationHistoryService;
    }
    
    /**
     * Test Groq API directly
     */
    @PostMapping("/groq")
    public ResponseEntity<Map<String, Object>> testGroqAPI(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "Hello, how are you doing today?");
        String phoneNumber = request.getOrDefault("phoneNumber", "+1234567890");
        
        Map<String, Object> response = new HashMap<>();
        response.put("input_message", message);
        response.put("groq_configured", groqService.isConfigured());
        
        try {
            // Create a simple conversation history
            List<ConversationHistoryService.ChatMessage> history = new ArrayList<>();
            
            // Get AI response
            String aiResponse = groqService.generateCompanionResponse(history, message);
            
            response.put("ai_response", aiResponse);
            response.put("status", "success");
            response.put("response_source", groqService.isConfigured() ? "groq_api" : "fallback");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
            response.put("ai_response", "Error generating response: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check Groq configuration status
     */
    @GetMapping("/groq/status")
    public ResponseEntity<Map<String, Object>> getGroqStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("groq_configured", groqService.isConfigured());
        status.put("service", "Groq AI Service");
        status.put("fallback_enabled", true);
        status.put("model", "llama-3.3-70b-versatile");
        
        return ResponseEntity.ok(status);
    }
}