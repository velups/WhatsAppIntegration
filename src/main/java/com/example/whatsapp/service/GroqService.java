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
import java.util.ArrayList;
import java.util.List;

@Service
public class GroqService {
    
    private static final Logger log = LoggerFactory.getLogger(GroqService.class);
    
    @Value("${groq.api.key:}")
    private String groqApiKey;
    
    @Value("${groq.api.base-url:https://api.groq.com/openai/v1}")
    private String groqApiBaseUrl;
    
    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;
    
    @Value("${groq.max-tokens:500}")
    private Integer maxTokens;
    
    @Value("${groq.temperature:0.8}")
    private Double temperature;
    
    @Value("${groq.timeout:30}")
    private Integer timeoutSeconds;
    
    private WebClient webClient;
    
    private static final String COMPANION_SYSTEM_PROMPT = """
        You are a caring, warm, and empathetic AI companion chatting with an elderly person through WhatsApp. 
        Your personality traits:
        - Always address them warmly as "Aunty" or use their name if provided
        - Be patient, understanding, and genuinely interested in their well-being
        - Share in their joys and provide comfort during difficulties
        - Ask thoughtful follow-up questions about their day, family, health, and interests
        - Remember this is a conversation, so keep responses conversational and not too long
        - Use simple, clear language that's easy to understand
        - Show genuine care and concern for their physical and emotional well-being
        - Occasionally share uplifting thoughts or gentle humor when appropriate
        - Be supportive and encouraging, especially if they share concerns or worries
        - If they mention feeling lonely or isolated, be extra supportive and engaging
        
        Important guidelines:
        - Keep responses concise (2-3 sentences usually) unless they ask for more detail
        - Always maintain a warm, caring, and respectful tone
        - Never provide medical advice, but encourage them to consult healthcare providers when needed
        - Be culturally sensitive and respectful of their experiences and wisdom
        """;
    
    @PostConstruct
    public void init() {
        if (groqApiKey != null && !groqApiKey.isEmpty() && !groqApiKey.equals("YOUR_GROQ_API_KEY_HERE")) {
            this.webClient = WebClient.builder()
                    .baseUrl(groqApiBaseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            log.info("Groq service initialized with model: {}", model);
        } else {
            log.warn("Groq API key not configured. AI features will be disabled.");
        }
    }
    
    /**
     * Generate a companion response based on the conversation history
     */
    public String generateCompanionResponse(List<ConversationHistoryService.ChatMessage> conversationHistory, String userMessage) {
        if (webClient == null) {
            log.warn("Groq service not initialized. Returning fallback response.");
            return "Hello Aunty! I'm here to chat with you. How are you feeling today? Please tell me about your day!";
        }
        
        try {
            // Convert conversation history to Groq format
            List<GroqRequest.Message> messages = new ArrayList<>();
            
            // Add system prompt
            messages.add(GroqRequest.Message.builder()
                    .role("system")
                    .content(COMPANION_SYSTEM_PROMPT)
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
            
            // Create Groq request
            GroqRequest request = GroqRequest.builder()
                    .messages(messages)
                    .model(model)
                    .max_tokens(maxTokens)
                    .temperature(temperature)
                    .stream(false)
                    .build();
            
            // Call Groq API
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
                log.info("Generated Groq AI response: {}", aiResponse);
                return aiResponse;
            } else {
                log.error("No response from Groq API");
                return getFallbackResponse();
            }
            
        } catch (Exception e) {
            log.error("Error generating AI response from Groq", e);
            return getFallbackResponse();
        }
    }
    
    /**
     * Generate the initial greeting for first-time users
     */
    public String generateInitialGreeting(String phoneNumber) {
        return "Hello Aunty! 🌺 How are you doing today? I'm here to chat with you and keep you company. Please tell me, how has your day been so far?";
    }
    
    /**
     * Get a fallback response when AI is not available
     */
    private String getFallbackResponse() {
        return "Thank you for your message, Aunty! I'm here to listen and chat with you. Please tell me more about how you're feeling today.";
    }
    
    /**
     * Check if Groq service is properly configured
     */
    public boolean isConfigured() {
        return webClient != null;
    }
}