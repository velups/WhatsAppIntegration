package com.example.whatsapp.service;

import com.example.whatsapp.dto.SentimentAnalysis;
import com.example.whatsapp.dto.GroqRequest;
import com.example.whatsapp.dto.GroqResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class SentimentAnalysisService {
    
    private static final Logger log = LoggerFactory.getLogger(SentimentAnalysisService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${groq.api.key:}")
    private String groqApiKey;
    
    @Value("${groq.api.base-url:https://api.groq.com/openai/v1}")
    private String groqApiBaseUrl;
    
    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;
    
    private static final String SENTIMENT_ANALYSIS_PROMPT = """
        You are an expert in analyzing the emotional well-being of elderly individuals through their text messages. 
        
        Analyze the following message from an elderly person and categorize their emotional state:
        
        Categories:
        - GREEN: Positive emotions (happy, content, grateful, excited, peaceful, joyful)
        - AMBER: Neutral or mixed emotions (slight worry, minor complaints, general conversation, mild concerns)
        - RED: Negative emotions requiring attention (sad, lonely, depressed, anxious, in pain, distressed, angry, hopeless)
        
        User Message: "{USER_MESSAGE}"
        
        Respond ONLY with valid JSON in this exact format:
        {
          "category": "GREEN|AMBER|RED",
          "confidence": 0.85,
          "emotional_indicators": "specific words/phrases that indicate emotion",
          "concern_level": "Low|Medium|High",
          "reasoning": "brief explanation of the analysis"
        }
        """;
    
    public SentimentAnalysis analyzeSentiment(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return SentimentAnalysis.amber(0.5, "Empty message", "No content to analyze");
        }
        
        try {
            if (isValidGroqConfig()) {
                return analyzeWithAI(userMessage);
            } else {
                return analyzeWithRules(userMessage);
            }
        } catch (Exception e) {
            log.warn("Sentiment analysis failed, using rule-based fallback: {}", e.getMessage());
            return analyzeWithRules(userMessage);
        }
    }
    
    private SentimentAnalysis analyzeWithAI(String userMessage) {
        try {
            WebClient webClient = WebClient.builder()
                    .baseUrl(groqApiBaseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + groqApiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            String prompt = SENTIMENT_ANALYSIS_PROMPT.replace("{USER_MESSAGE}", userMessage);
            
            List<GroqRequest.Message> messages = new ArrayList<>();
            messages.add(GroqRequest.Message.builder()
                    .role("user")
                    .content(prompt)
                    .build());
            
            GroqRequest request = GroqRequest.builder()
                    .messages(messages)
                    .model(model)
                    .max_tokens(200)
                    .temperature(0.3) // Lower temperature for more consistent analysis
                    .stream(false)
                    .build();
            
            GroqResponse response = webClient
                    .post()
                    .uri("/chat/completions")
                    .body(Mono.just(request), GroqRequest.class)
                    .retrieve()
                    .bodyToMono(GroqResponse.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String aiResponse = response.getChoices().get(0).getMessage().getContent();
                return parseSentimentResponse(aiResponse);
            }
            
        } catch (Exception e) {
            log.error("AI sentiment analysis failed: {}", e.getMessage());
            throw e;
        }
        
        return analyzeWithRules(userMessage);
    }
    
    private SentimentAnalysis parseSentimentResponse(String aiResponse) {
        try {
            // Clean the response to extract JSON
            String jsonStr = aiResponse.trim();
            if (jsonStr.contains("```json")) {
                jsonStr = jsonStr.substring(jsonStr.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (jsonStr.contains("{")) {
                jsonStr = jsonStr.substring(jsonStr.indexOf("{"));
                jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("}") + 1);
            }
            
            JsonNode json = objectMapper.readTree(jsonStr);
            
            String categoryStr = json.get("category").asText();
            SentimentAnalysis.SentimentCategory category = SentimentAnalysis.SentimentCategory.valueOf(categoryStr);
            Double confidence = json.get("confidence").asDouble();
            String indicators = json.get("emotional_indicators").asText();
            String reasoning = json.get("reasoning").asText();
            
            log.info("AI Sentiment Analysis - Category: {}, Confidence: {}, Indicators: {}", 
                    category, confidence, indicators);
            
            return new SentimentAnalysis(category, confidence, indicators, 
                    getConcernLevel(category), reasoning);
            
        } catch (Exception e) {
            log.warn("Failed to parse AI sentiment response, using fallback: {}", e.getMessage());
            return analyzeWithRules(aiResponse);
        }
    }
    
    private SentimentAnalysis analyzeWithRules(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // Red flags - negative emotions requiring attention
        if (containsAny(lowerMessage, "sad", "depressed", "lonely", "hurt", "pain", "angry", 
                       "upset", "crying", "hopeless", "terrible", "awful", "horrible",
                       "can't take", "give up", "want to die", "hate", "miserable",
                       "anxious", "worried sick", "panic", "scared", "terrified")) {
            return SentimentAnalysis.red(0.8, extractKeywords(lowerMessage, 
                    "sad", "depressed", "lonely", "hurt", "pain", "angry", "upset"), 
                    "Negative emotional indicators detected");
        }
        
        // Green flags - positive emotions
        if (containsAny(lowerMessage, "happy", "good", "great", "wonderful", "excellent",
                       "love", "joy", "grateful", "thankful", "blessed", "excited",
                       "amazing", "beautiful", "perfect", "fantastic", "delighted",
                       "content", "peaceful", "proud", "thrilled")) {
            return SentimentAnalysis.green(0.7, extractKeywords(lowerMessage,
                    "happy", "good", "great", "love", "grateful", "blessed"),
                    "Positive emotional indicators detected");
        }
        
        // Amber flags - neutral or mild concerns
        if (containsAny(lowerMessage, "okay", "fine", "alright", "tired", "busy",
                       "not bad", "could be better", "so-so", "getting by",
                       "little worried", "bit concerned", "not sure")) {
            return SentimentAnalysis.amber(0.6, extractKeywords(lowerMessage,
                    "okay", "fine", "tired", "worried", "concerned"),
                    "Neutral or mild emotional indicators");
        }
        
        // Default to amber for general conversation
        return SentimentAnalysis.amber(0.5, "General conversation", 
                "No strong emotional indicators detected");
    }
    
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String extractKeywords(String text, String... keywords) {
        List<String> found = new ArrayList<>();
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                found.add(keyword);
            }
        }
        return String.join(", ", found);
    }
    
    private String getConcernLevel(SentimentAnalysis.SentimentCategory category) {
        return switch (category) {
            case GREEN -> "Low";
            case AMBER -> "Medium";
            case RED -> "High";
        };
    }
    
    private boolean isValidGroqConfig() {
        return groqApiKey != null && !groqApiKey.isEmpty() && 
               !groqApiKey.equals("YOUR_GROQ_API_KEY_HERE");
    }
}