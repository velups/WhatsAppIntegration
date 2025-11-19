package com.example.whatsapp.service;

import com.example.whatsapp.dto.GroqRequest;
import com.example.whatsapp.dto.GroqResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.*;

@Service
public class AIProviderService {
    
    private static final Logger log = LoggerFactory.getLogger(AIProviderService.class);
    
    // Configuration
    @Value("${ai.primary.provider:groq}")
    private String primaryProvider;
    
    @Value("${ai.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    // Groq Configuration
    @Value("${groq.api.key:}")
    private String groqApiKey;
    
    @Value("${groq.api.base-url:https://api.groq.com/openai/v1}")
    private String groqApiBaseUrl;
    
    @Value("${groq.model:mixtral-8x7b-32768}")
    private String groqModel;
    
    // Hugging Face Configuration
    @Value("${huggingface.api.key:}")
    private String hfApiKey;
    
    @Value("${huggingface.api.base-url:https://api-inference.huggingface.co/models}")
    private String hfApiBaseUrl;
    
    @Value("${huggingface.model:microsoft/DialoGPT-medium}")
    private String hfModel;
    
    // OpenRouter Configuration (free tier available)
    @Value("${openrouter.api.key:}")
    private String openrouterApiKey;
    
    @Value("${openrouter.api.base-url:https://openrouter.ai/api/v1}")
    private String openrouterApiBaseUrl;
    
    @Value("${openrouter.model:meta-llama/llama-3.2-3b-instruct:free}")
    private String openrouterModel;
    
    // Together AI Configuration (has free tier)
    @Value("${together.api.key:}")
    private String togetherApiKey;
    
    @Value("${together.api.base-url:https://api.together.xyz/v1}")
    private String togetherApiBaseUrl;
    
    @Value("${together.model:meta-llama/Llama-3.2-3B-Instruct-Turbo}")
    private String togetherModel;
    
    @Value("${ai.max-tokens:500}")
    private Integer maxTokens;
    
    @Value("${ai.temperature:0.8}")
    private Double temperature;
    
    @Value("${ai.timeout:10}")
    private Integer timeoutSeconds;
    
    private final Map<String, ProviderConfig> providers = new HashMap<>();
    private final List<String> providerOrder = Arrays.asList("groq", "openrouter", "together", "huggingface");
    
    @PostConstruct
    public void init() {
        initializeProviders();
        logAvailableProviders();
    }
    
    private void initializeProviders() {
        // Initialize Groq
        if (isValidApiKey(groqApiKey)) {
            providers.put("groq", new ProviderConfig(
                "Groq", groqApiBaseUrl, groqApiKey, groqModel, "openai"
            ));
        }
        
        // Initialize OpenRouter (has free models)
        if (isValidApiKey(openrouterApiKey)) {
            providers.put("openrouter", new ProviderConfig(
                "OpenRouter", openrouterApiBaseUrl, openrouterApiKey, openrouterModel, "openai"
            ));
        }
        
        // Initialize Together AI
        if (isValidApiKey(togetherApiKey)) {
            providers.put("together", new ProviderConfig(
                "Together AI", togetherApiBaseUrl, togetherApiKey, togetherModel, "openai"
            ));
        }
        
        // Initialize Hugging Face (Deprecated - Inference API no longer supported)
        // Commenting out due to API deprecation as of Nov 2024
        // if (isValidApiKey(hfApiKey)) {
        //     providers.put("huggingface", new ProviderConfig(
        //         "Hugging Face", hfApiBaseUrl, hfApiKey, hfModel, "huggingface"
        //     ));
        // }
    }
    
    private boolean isValidApiKey(String apiKey) {
        return apiKey != null && !apiKey.isEmpty() && 
               !apiKey.equals("YOUR_API_KEY_HERE") && 
               !apiKey.equals("YOUR_GROQ_API_KEY_HERE");
    }
    
    private void logAvailableProviders() {
        if (providers.isEmpty()) {
            log.warn("No AI providers configured. Using fallback responses only.");
        } else {
            log.info("Available AI providers: {}", providers.keySet());
            log.info("Primary provider: {}", primaryProvider);
            log.info("Fallback enabled: {}", fallbackEnabled);
        }
    }
    
    /**
     * Generate AI response with automatic failover
     */
    public String generateResponse(List<ConversationHistoryService.ChatMessage> conversationHistory, String userMessage) {
        // Try primary provider first
        if (providers.containsKey(primaryProvider)) {
            try {
                String response = callProvider(primaryProvider, conversationHistory, userMessage);
                if (response != null) {
                    log.debug("Response generated using primary provider: {}", primaryProvider);
                    return response;
                }
            } catch (Exception e) {
                log.warn("Primary provider {} failed: {}", primaryProvider, e.getMessage());
            }
        }
        
        // Try other providers in order
        if (fallbackEnabled) {
            for (String providerName : providerOrder) {
                if (!providerName.equals(primaryProvider) && providers.containsKey(providerName)) {
                    try {
                        String response = callProvider(providerName, conversationHistory, userMessage);
                        if (response != null) {
                            log.info("Response generated using fallback provider: {}", providerName);
                            return response;
                        }
                    } catch (Exception e) {
                        log.warn("Fallback provider {} failed: {}", providerName, e.getMessage());
                    }
                }
            }
        }
        
        // All providers failed, use intelligent fallback
        log.info("All AI providers failed, using intelligent fallback");
        return generateIntelligentFallback(userMessage);
    }
    
    private String callProvider(String providerName, List<ConversationHistoryService.ChatMessage> conversationHistory, String userMessage) {
        ProviderConfig config = providers.get(providerName);
        if (config == null) {
            return null;
        }
        
        if ("openai".equals(config.apiType)) {
            return callOpenAICompatibleProvider(config, conversationHistory, userMessage);
        } else if ("huggingface".equals(config.apiType)) {
            return callHuggingFaceProvider(config, userMessage);
        }
        
        return null;
    }
    
    private String callOpenAICompatibleProvider(ProviderConfig config, List<ConversationHistoryService.ChatMessage> conversationHistory, String userMessage) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(config.baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            // Prepare messages
            List<GroqRequest.Message> messages = new ArrayList<>();
            
            // Add system prompt
            messages.add(GroqRequest.Message.builder()
                    .role("system")
                    .content(getSystemPrompt())
                    .build());
            
            // Add conversation history
            for (ConversationHistoryService.ChatMessage chatMessage : conversationHistory) {
                messages.add(GroqRequest.Message.builder()
                        .role(chatMessage.getRole())
                        .content(chatMessage.getContent())
                        .build());
            }
            
            // Add current user message
            messages.add(GroqRequest.Message.builder()
                    .role("user")
                    .content(userMessage)
                    .build());
            
            // Create request
            GroqRequest request = GroqRequest.builder()
                    .messages(messages)
                    .model(config.model)
                    .max_tokens(maxTokens)
                    .temperature(temperature)
                    .stream(false)
                    .build();
            
            // Call API
            GroqResponse response = webClient
                    .post()
                    .uri("/chat/completions")
                    .body(Mono.just(request), GroqRequest.class)
                    .retrieve()
                    .bodyToMono(GroqResponse.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String aiResponse = response.getChoices().get(0).getMessage().getContent();
                log.debug("Generated response from {}: {}", config.name, aiResponse);
                return aiResponse;
            }
            
        } catch (Exception e) {
            log.error("Error calling provider {}: {}", config.name, e.getMessage());
            throw e;
        }
        
        return null;
    }
    
    private String callHuggingFaceProvider(ProviderConfig config, String userMessage) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(config.baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            Map<String, Object> request = new HashMap<>();
            request.put("inputs", userMessage);
            request.put("parameters", Map.of("max_length", maxTokens, "temperature", temperature));
            
            // Hugging Face API has different response format
            var response = webClient
                    .post()
                    .uri("/" + config.model)
                    .body(Mono.just(request), Map.class)
                    .retrieve()
                    .bodyToFlux(Map.class)
                    .collectList()
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            
            if (response != null && !response.isEmpty()) {
                Map<String, Object> result = response.get(0);
                String generatedText = (String) result.get("generated_text");
                if (generatedText != null) {
                    // Clean up the response
                    generatedText = generatedText.replace(userMessage, "").trim();
                    if (!generatedText.isEmpty()) {
                        return generatedText;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error calling Hugging Face: {}", e.getMessage());
            throw e;
        }
        
        return null;
    }
    
    private String generateIntelligentFallback(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();

        // Detect language based on character ranges and common words
        boolean isTamil = userMessage.matches(".*[\\u0B80-\\u0BFF].*");
        boolean isChinese = userMessage.matches(".*[\\u4E00-\\u9FFF].*");
        boolean isMalay = lowerMessage.contains("apa") || lowerMessage.contains("saya") || lowerMessage.contains("terima kasih");
        boolean isHindi = userMessage.matches(".*[\\u0900-\\u097F].*");

        // Tamil responses
        if (isTamil) {
            if (lowerMessage.contains("வணக்கம்")) {
                return "வணக்கம்! 🌺 நீங்கள் எப்படி இருக்கிறீர்கள்? உங்களிடம் பேச மகிழ்ச்சி!";
            }
            return "நன்றி! 🌺 நான் உங்களுடன் பேச இங்கே இருக்கிறேன். இன்று நீங்கள் எப்படி உணர்கிறீர்கள்?";
        }

        // Chinese responses
        if (isChinese) {
            if (userMessage.contains("你好") || userMessage.contains("您好")) {
                return "你好！🌺 您今天好吗？很高兴收到您的消息！";
            }
            return "谢谢您的分享！🌺 我在这里陪您聊天。您今天感觉怎么样？";
        }

        // Malay responses
        if (isMalay) {
            if (lowerMessage.contains("apa khabar") || lowerMessage.contains("hello")) {
                return "Hai! 🌺 Apa khabar hari ini? Saya gembira dapat bercakap dengan awak!";
            }
            return "Terima kasih kerana berkongsi! 🌺 Saya di sini untuk berbual. Bagaimana perasaan awak hari ini?";
        }

        // Hindi responses
        if (isHindi) {
            if (userMessage.contains("नमस्ते")) {
                return "नमस्ते! 🌺 आप कैसे हैं? आपसे बात करके खुशी हुई!";
            }
            return "धन्यवाद! 🌺 मैं आपसे बात करने के लिए यहां हूं। आज आप कैसा महसूस कर रहे हैं?";
        }

        // English responses (default)
        if (lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "Hello! 🌺 How are you doing today? I'm so happy to hear from you!";
        } else if (lowerMessage.contains("good") || lowerMessage.contains("fine") || lowerMessage.contains("ok")) {
            return "That's wonderful to hear! I'm so glad you're doing well. Tell me more about your day!";
        } else if (lowerMessage.contains("tired") || lowerMessage.contains("sad") || lowerMessage.contains("not good")) {
            return "Oh dear, I'm sorry to hear that. I'm here for you. Would you like to tell me what's on your mind?";
        } else if (lowerMessage.contains("thank")) {
            return "You're so welcome! It's my pleasure to chat with you. How else can I brighten your day?";
        } else if (lowerMessage.contains("weather")) {
            return "I hope the weather is lovely where you are! Are you able to enjoy some fresh air today?";
        } else if (lowerMessage.contains("food") || lowerMessage.contains("eat")) {
            return "I hope you're eating well! Have you had something delicious today? Taking care of yourself is so important.";
        } else {
            return "Thank you for sharing that with me! I'm here to listen and chat with you. How are you feeling today?";
        }
    }
    
    private String getSystemPrompt() {
        return """
            You are a caring, warm, and empathetic AI companion chatting with an elderly person through WhatsApp.

            CRITICAL LANGUAGE RULE:
            - ALWAYS detect the language of the user's message
            - ALWAYS respond in the SAME language the user wrote in
            - If they write in Tamil, respond in Tamil
            - If they write in Mandarin/Chinese, respond in Chinese
            - If they write in Malay, respond in Malay
            - If they write in Hindi, respond in Hindi
            - If they write in English, respond in English
            - If they mix languages (e.g., Singlish, Tanglish), match their style

            Your personality traits:
            - Always address them warmly using their name if provided, or appropriate respectful terms in their language
            - Be patient, understanding, and genuinely interested in their well-being
            - Share in their joys and provide comfort during difficulties
            - Ask thoughtful follow-up questions about their day, family, health, and interests
            - Keep responses conversational and warm (2-3 sentences max)
            - Always maintain a warm, caring, and respectful tone
            - Never provide medical advice, but encourage them to consult healthcare providers when needed
            - Be culturally sensitive and respectful of their experiences and wisdom
            """;
    }
    
    /**
     * Get current provider status
     */
    public Map<String, Object> getProviderStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("primary_provider", primaryProvider);
        status.put("fallback_enabled", fallbackEnabled);
        status.put("available_providers", providers.keySet());
        status.put("total_providers", providers.size());
        
        Map<String, String> providerDetails = new HashMap<>();
        for (Map.Entry<String, ProviderConfig> entry : providers.entrySet()) {
            providerDetails.put(entry.getKey(), entry.getValue().name + " (" + entry.getValue().model + ")");
        }
        status.put("provider_details", providerDetails);
        
        return status;
    }
    
    /**
     * Switch primary provider
     */
    public boolean switchPrimaryProvider(String newProvider) {
        if (providers.containsKey(newProvider)) {
            this.primaryProvider = newProvider;
            log.info("Primary AI provider switched to: {}", newProvider);
            return true;
        } else {
            log.warn("Cannot switch to provider {}: not configured or available", newProvider);
            return false;
        }
    }
    
    /**
     * Check if any providers are configured
     */
    public boolean hasAvailableProviders() {
        return !providers.isEmpty();
    }
    
    /**
     * Get initial greeting
     */
    public String generateInitialGreeting(String phoneNumber) {
        return "Hello Aunty! 🌺 How are you doing today? I'm here to chat with you and keep you company. Please tell me, how has your day been so far?";
    }
    
    private static class ProviderConfig {
        final String name;
        final String baseUrl;
        final String apiKey;
        final String model;
        final String apiType;
        
        ProviderConfig(String name, String baseUrl, String apiKey, String model, String apiType) {
            this.name = name;
            this.baseUrl = baseUrl;
            this.apiKey = apiKey;
            this.model = model;
            this.apiType = apiType;
        }
    }
}