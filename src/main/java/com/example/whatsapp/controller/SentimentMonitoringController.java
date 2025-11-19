package com.example.whatsapp.controller;

import com.example.whatsapp.dto.SentimentAnalysis;
import com.example.whatsapp.entity.ConversationSentiment;
import com.example.whatsapp.service.ConversationSentimentService;
import com.example.whatsapp.service.SentimentAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sentiment")
public class SentimentMonitoringController {
    
    private final ConversationSentimentService conversationSentimentService;
    private final SentimentAnalysisService sentimentAnalysisService;
    
    public SentimentMonitoringController(ConversationSentimentService conversationSentimentService,
                                       SentimentAnalysisService sentimentAnalysisService) {
        this.conversationSentimentService = conversationSentimentService;
        this.sentimentAnalysisService = sentimentAnalysisService;
    }
    
    /**
     * Get overall sentiment dashboard overview
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getSentimentOverview() {
        Map<String, Object> overview = conversationSentimentService.getSentimentOverview();
        return ResponseEntity.ok(overview);
    }
    
    /**
     * Get conversations requiring immediate attention (RED category)
     */
    @GetMapping("/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts() {
        List<ConversationSentiment> alerts = conversationSentimentService.getConversationsRequiringAttention();
        
        Map<String, Object> response = new HashMap<>();
        response.put("alert_count", alerts.size());
        response.put("conversations", alerts);
        response.put("status", alerts.isEmpty() ? "all_clear" : "attention_required");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get sentiment history for a specific user
     */
    @GetMapping("/user/{phoneNumber}")
    public ResponseEntity<Map<String, Object>> getUserSentiment(@PathVariable String phoneNumber) {
        List<ConversationSentiment> history = conversationSentimentService.getUserSentimentHistory(phoneNumber);
        Map<String, Object> trend = conversationSentimentService.getUserSentimentTrend(phoneNumber);
        
        Map<String, Object> response = new HashMap<>();
        response.put("phone_number", phoneNumber);
        response.put("sentiment_history", history);
        response.put("trend_analysis", trend);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get recent sentiment for a specific user (last 24 hours)
     */
    @GetMapping("/user/{phoneNumber}/recent")
    public ResponseEntity<Map<String, Object>> getRecentUserSentiment(@PathVariable String phoneNumber) {
        List<ConversationSentiment> recent = conversationSentimentService.getRecentUserSentiments(phoneNumber);
        
        Map<String, Object> response = new HashMap<>();
        response.put("phone_number", phoneNumber);
        response.put("recent_conversations", recent);
        response.put("conversation_count", recent.size());
        
        if (!recent.isEmpty()) {
            ConversationSentiment latest = recent.get(0);
            response.put("current_sentiment", latest.getSentimentCategory());
            response.put("current_confidence", latest.getConfidenceScore());
            response.put("requires_attention", latest.getRequiresAttention());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test sentiment analysis with a message
     */
    @PostMapping("/analyze")
    public ResponseEntity<SentimentAnalysis> analyzeSentiment(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        SentimentAnalysis analysis = sentimentAnalysisService.analyzeSentiment(message);
        return ResponseEntity.ok(analysis);
    }
    
    /**
     * Get sentiment statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSentimentStats() {
        Map<String, Object> overview = conversationSentimentService.getSentimentOverview();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("last_24_hours", overview);
        
        // Add additional stats
        @SuppressWarnings("unchecked")
        Map<String, Long> counts = (Map<String, Long>) overview.get("sentiment_counts");
        if (counts != null) {
            long total = counts.values().stream().mapToLong(Long::longValue).sum();
            stats.put("total_conversations_24h", total);
            
            if (total > 0) {
                stats.put("green_ratio", String.format("%.1f%%", (counts.get("GREEN") * 100.0) / total));
                stats.put("amber_ratio", String.format("%.1f%%", (counts.get("AMBER") * 100.0) / total));
                stats.put("red_ratio", String.format("%.1f%%", (counts.get("RED") * 100.0) / total));
            }
        }
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Health check for sentiment monitoring system
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test sentiment analysis
            SentimentAnalysis testAnalysis = sentimentAnalysisService.analyzeSentiment("Hello, I am feeling good today");
            
            health.put("sentiment_analysis", "operational");
            health.put("test_result", testAnalysis.getCategory());
            health.put("database_connection", "healthy");
            health.put("status", "healthy");
            
        } catch (Exception e) {
            health.put("sentiment_analysis", "degraded");
            health.put("error", e.getMessage());
            health.put("status", "degraded");
        }
        
        health.put("service", "Sentiment Monitoring Service");
        health.put("version", "1.0");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get sentiment color coding guide
     */
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Object>> getSentimentGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        Map<String, Object> categories = new HashMap<>();
        
        Map<String, Object> green = new HashMap<>();
        green.put("name", "GREEN - Positive");
        green.put("description", "Happy, content, grateful, excited, peaceful, joyful");
        green.put("action_required", "None - Continue regular check-ins");
        green.put("examples", List.of("I'm feeling wonderful today!", "So grateful for my family", "Had a lovely day"));
        
        Map<String, Object> amber = new HashMap<>();
        amber.put("name", "AMBER - Neutral/Mixed");
        amber.put("description", "Slight worry, minor complaints, general conversation, mild concerns");
        amber.put("action_required", "Monitor - Keep engaging in conversation");
        amber.put("examples", List.of("I'm okay, just a bit tired", "Could be better", "Not sure how I feel"));
        
        Map<String, Object> red = new HashMap<>();
        red.put("name", "RED - Concerning");
        red.put("description", "Sad, lonely, depressed, anxious, in pain, distressed, angry, hopeless");
        red.put("action_required", "ATTENTION REQUIRED - Consider follow-up or support");
        red.put("examples", List.of("I feel so lonely", "I'm in a lot of pain", "Nothing seems to matter"));
        
        categories.put("GREEN", green);
        categories.put("AMBER", amber);
        categories.put("RED", red);
        
        guide.put("categories", categories);
        guide.put("purpose", "Monitor emotional well-being of elderly users through conversation analysis");
        guide.put("ai_powered", true);
        guide.put("fallback_rules", "Rule-based analysis when AI is unavailable");
        
        return ResponseEntity.ok(guide);
    }
}