package com.example.whatsapp.controller;

import com.example.whatsapp.service.AIProviderService;
import com.example.whatsapp.service.ConversationHistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIProviderController {
    
    private final AIProviderService aiProviderService;
    private final ConversationHistoryService conversationHistoryService;
    
    public AIProviderController(AIProviderService aiProviderService, ConversationHistoryService conversationHistoryService) {
        this.aiProviderService = aiProviderService;
        this.conversationHistoryService = conversationHistoryService;
    }
    
    /**
     * Get current AI provider status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProviderStatus() {
        return ResponseEntity.ok(aiProviderService.getProviderStatus());
    }
    
    /**
     * Switch primary AI provider
     */
    @PostMapping("/switch")
    public ResponseEntity<Map<String, Object>> switchProvider(@RequestBody Map<String, String> request) {
        String newProvider = request.get("provider");
        boolean success = aiProviderService.switchPrimaryProvider(newProvider);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? 
            "Primary provider switched to " + newProvider : 
            "Failed to switch to " + newProvider + ". Provider not available.");
        response.put("current_status", aiProviderService.getProviderStatus());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test AI provider with a message
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testProvider(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "Hello, how are you doing today?");
        String phoneNumber = request.getOrDefault("phoneNumber", "+1234567890");
        
        Map<String, Object> response = new HashMap<>();
        response.put("input_message", message);
        response.put("provider_status", aiProviderService.getProviderStatus());
        
        try {
            // Create a simple conversation history
            List<ConversationHistoryService.ChatMessage> history = new ArrayList<>();
            
            // Get AI response
            long startTime = System.currentTimeMillis();
            String aiResponse = aiProviderService.generateResponse(history, message);
            long responseTime = System.currentTimeMillis() - startTime;
            
            response.put("ai_response", aiResponse);
            response.put("status", "success");
            response.put("response_time_ms", responseTime);
            response.put("has_providers", aiProviderService.hasAvailableProviders());
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
            response.put("ai_response", "Error generating response: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get list of supported providers and their setup instructions
     */
    @GetMapping("/providers")
    public ResponseEntity<Map<String, Object>> getSupportedProviders() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Map<String, Object>> providers = new HashMap<>();
        
        // Groq
        Map<String, Object> groq = new HashMap<>();
        groq.put("name", "Groq");
        groq.put("description", "Fast inference for open source LLMs");
        groq.put("free_tier", true);
        groq.put("signup_url", "https://console.groq.com/keys");
        groq.put("models", Arrays.asList("llama-3.3-70b-versatile", "llama-3.1-70b-versatile"));
        groq.put("env_var", "GROQ_API_KEY");
        providers.put("groq", groq);
        
        // OpenRouter
        Map<String, Object> openrouter = new HashMap<>();
        openrouter.put("name", "OpenRouter");
        openrouter.put("description", "Access to multiple AI models including free ones");
        openrouter.put("free_tier", true);
        openrouter.put("signup_url", "https://openrouter.ai/keys");
        openrouter.put("models", Arrays.asList("meta-llama/llama-3.2-3b-instruct:free", "mistralai/mistral-7b-instruct:free"));
        openrouter.put("env_var", "OPENROUTER_API_KEY");
        providers.put("openrouter", openrouter);
        
        // Together AI
        Map<String, Object> together = new HashMap<>();
        together.put("name", "Together AI");
        together.put("description", "Fast inference for open source models");
        together.put("free_tier", true);
        together.put("signup_url", "https://api.together.xyz/settings/api-keys");
        together.put("models", Arrays.asList("meta-llama/Llama-3.2-3B-Instruct-Turbo", "mistralai/Mistral-7B-Instruct-v0.1"));
        together.put("env_var", "TOGETHER_API_KEY");
        providers.put("together", together);
        
        // Hugging Face (Deprecated)
        Map<String, Object> huggingface = new HashMap<>();
        huggingface.put("name", "Hugging Face");
        huggingface.put("description", "DEPRECATED: Inference API no longer supported as of Nov 2024");
        huggingface.put("free_tier", false);
        huggingface.put("signup_url", "https://huggingface.co/settings/tokens");
        huggingface.put("models", Arrays.asList("DEPRECATED"));
        huggingface.put("env_var", "HUGGINGFACE_API_KEY");
        huggingface.put("status", "deprecated");
        providers.put("huggingface", huggingface);
        
        response.put("supported_providers", providers);
        response.put("current_status", aiProviderService.getProviderStatus());
        
        Map<String, String> instructions = new HashMap<>();
        instructions.put("setup", "Configure environment variables for the providers you want to use");
        instructions.put("primary", "Set ai.primary.provider in application.properties");
        instructions.put("failover", "Enable ai.fallback.enabled=true for automatic failover");
        response.put("setup_instructions", instructions);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check for AI providers
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("has_providers", aiProviderService.hasAvailableProviders());
        health.put("provider_count", aiProviderService.getProviderStatus().get("total_providers"));
        health.put("primary_provider", aiProviderService.getProviderStatus().get("primary_provider"));
        health.put("fallback_enabled", aiProviderService.getProviderStatus().get("fallback_enabled"));
        health.put("status", aiProviderService.hasAvailableProviders() ? "healthy" : "degraded");
        health.put("service", "AI Provider Service");
        
        return ResponseEntity.ok(health);
    }
}