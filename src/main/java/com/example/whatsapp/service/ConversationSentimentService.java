package com.example.whatsapp.service;

import com.example.whatsapp.dto.SentimentAnalysis;
import com.example.whatsapp.entity.ConversationSentiment;
import com.example.whatsapp.repository.ConversationSentimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationSentimentService {
    
    private static final Logger log = LoggerFactory.getLogger(ConversationSentimentService.class);
    
    private final ConversationSentimentRepository sentimentRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    
    public ConversationSentimentService(ConversationSentimentRepository sentimentRepository,
                                      SentimentAnalysisService sentimentAnalysisService) {
        this.sentimentRepository = sentimentRepository;
        this.sentimentAnalysisService = sentimentAnalysisService;
    }
    
    /**
     * Analyze and store sentiment for a conversation
     */
    public ConversationSentiment analyzeAndStoreSentiment(String phoneNumber, 
                                                        String userMessage, 
                                                        String aiResponse) {
        try {
            // Perform sentiment analysis
            SentimentAnalysis analysis = sentimentAnalysisService.analyzeSentiment(userMessage);
            
            // Create and save sentiment record
            ConversationSentiment sentiment = new ConversationSentiment(
                    phoneNumber, userMessage, aiResponse, analysis);
            
            ConversationSentiment saved = sentimentRepository.save(sentiment);
            
            // Log important sentiment changes
            if (analysis.getCategory() == SentimentAnalysis.SentimentCategory.RED) {
                log.warn("RED ALERT: User {} shows concerning sentiment: {} (confidence: {})", 
                        phoneNumber, analysis.getEmotionalIndicators(), analysis.getConfidence());
            } else if (analysis.getCategory() == SentimentAnalysis.SentimentCategory.GREEN) {
                log.info("POSITIVE: User {} shows positive sentiment: {} (confidence: {})", 
                        phoneNumber, analysis.getEmotionalIndicators(), analysis.getConfidence());
            }
            
            return saved;
            
        } catch (Exception e) {
            log.error("Failed to analyze sentiment for user {}: {}", phoneNumber, e.getMessage());
            // Still save a basic record without sentiment analysis
            ConversationSentiment basicSentiment = new ConversationSentiment();
            basicSentiment.setPhoneNumber(phoneNumber);
            basicSentiment.setUserMessage(userMessage);
            basicSentiment.setAiResponse(aiResponse);
            basicSentiment.setSentimentCategory(SentimentAnalysis.SentimentCategory.AMBER);
            basicSentiment.setConfidenceScore(0.0);
            basicSentiment.setReasoning("Analysis failed - manual review recommended");
            return sentimentRepository.save(basicSentiment);
        }
    }
    
    /**
     * Get sentiment history for a user
     */
    public List<ConversationSentiment> getUserSentimentHistory(String phoneNumber) {
        return sentimentRepository.findByPhoneNumberOrderByTimestampDesc(phoneNumber);
    }
    
    /**
     * Get recent sentiment history for a user (last 24 hours)
     */
    public List<ConversationSentiment> getRecentUserSentiments(String phoneNumber) {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return sentimentRepository.findByPhoneNumberAndTimestampAfterOrderByTimestampDesc(
                phoneNumber, yesterday);
    }
    
    /**
     * Get all conversations requiring attention (RED category)
     */
    public List<ConversationSentiment> getConversationsRequiringAttention() {
        return sentimentRepository.findByRequiresAttentionTrueOrderByTimestampDesc();
    }
    
    /**
     * Get sentiment overview for the last 24 hours
     */
    public Map<String, Object> getSentimentOverview() {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        
        Map<String, Object> overview = new HashMap<>();
        
        // Get recent sentiments
        List<ConversationSentiment> recentSentiments = sentimentRepository.findRecentSentiments(yesterday);
        overview.put("total_conversations", recentSentiments.size());
        
        // Count by category
        List<Object[]> sentimentCounts = sentimentRepository.getSentimentCounts(yesterday);
        Map<String, Long> categoryCounts = new HashMap<>();
        categoryCounts.put("GREEN", 0L);
        categoryCounts.put("AMBER", 0L);
        categoryCounts.put("RED", 0L);
        
        for (Object[] count : sentimentCounts) {
            String category = count[0].toString();
            Long num = (Long) count[1];
            categoryCounts.put(category, num);
        }
        
        overview.put("sentiment_counts", categoryCounts);
        
        // Users requiring attention
        List<String> usersNeedingAttention = sentimentRepository.findUsersWithRecentRedSentiments(yesterday);
        overview.put("users_needing_attention", usersNeedingAttention);
        overview.put("attention_count", usersNeedingAttention.size());
        
        // Calculate percentages
        long total = recentSentiments.size();
        if (total > 0) {
            Map<String, Double> percentages = new HashMap<>();
            percentages.put("green_percentage", (categoryCounts.get("GREEN") * 100.0) / total);
            percentages.put("amber_percentage", (categoryCounts.get("AMBER") * 100.0) / total);
            percentages.put("red_percentage", (categoryCounts.get("RED") * 100.0) / total);
            overview.put("percentages", percentages);
        }
        
        return overview;
    }
    
    /**
     * Get sentiment trends for a specific user
     */
    public Map<String, Object> getUserSentimentTrend(String phoneNumber) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<ConversationSentiment> sentiments = sentimentRepository.findUserSentimentsAfter(
                phoneNumber, weekAgo);
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("total_messages", sentiments.size());
        
        if (!sentiments.isEmpty()) {
            // Latest sentiment
            ConversationSentiment latest = sentiments.get(0);
            trend.put("current_sentiment", latest.getSentimentCategory());
            trend.put("latest_confidence", latest.getConfidenceScore());
            trend.put("latest_indicators", latest.getEmotionalIndicators());
            
            // Count categories in last week
            Map<String, Long> weekCounts = new HashMap<>();
            weekCounts.put("GREEN", 0L);
            weekCounts.put("AMBER", 0L);
            weekCounts.put("RED", 0L);
            
            for (ConversationSentiment s : sentiments) {
                String category = s.getSentimentCategory().toString();
                weekCounts.put(category, weekCounts.get(category) + 1);
            }
            
            trend.put("week_sentiment_counts", weekCounts);
            
            // Check for concerning patterns
            long redCount = weekCounts.get("RED");
            if (redCount > 0) {
                trend.put("concern_alert", true);
                trend.put("red_conversations_count", redCount);
            } else {
                trend.put("concern_alert", false);
            }
        }
        
        return trend;
    }
}