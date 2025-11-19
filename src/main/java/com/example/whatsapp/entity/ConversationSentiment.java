package com.example.whatsapp.entity;

import com.example.whatsapp.dto.SentimentAnalysis;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_sentiments")
public class ConversationSentiment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;
    
    @Column(name = "user_message", columnDefinition = "TEXT")
    private String userMessage;
    
    @Column(name = "ai_response", columnDefinition = "TEXT")
    private String aiResponse;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sentiment_category", nullable = false)
    private SentimentAnalysis.SentimentCategory sentimentCategory;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "emotional_indicators", columnDefinition = "TEXT")
    private String emotionalIndicators;
    
    @Column(name = "concern_level")
    private String concernLevel;
    
    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "requires_attention")
    private Boolean requiresAttention;
    
    public ConversationSentiment() {
        this.timestamp = LocalDateTime.now();
        this.requiresAttention = false;
    }
    
    public ConversationSentiment(String phoneNumber, String userMessage, String aiResponse, 
                               SentimentAnalysis sentimentAnalysis) {
        this();
        this.phoneNumber = phoneNumber;
        this.userMessage = userMessage;
        this.aiResponse = aiResponse;
        this.sentimentCategory = sentimentAnalysis.getCategory();
        this.confidenceScore = sentimentAnalysis.getConfidence();
        this.emotionalIndicators = sentimentAnalysis.getEmotionalIndicators();
        this.concernLevel = sentimentAnalysis.getConcernLevel();
        this.reasoning = sentimentAnalysis.getReasoning();
        this.requiresAttention = sentimentAnalysis.getCategory() == SentimentAnalysis.SentimentCategory.RED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getUserMessage() {
        return userMessage;
    }
    
    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }
    
    public String getAiResponse() {
        return aiResponse;
    }
    
    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }
    
    public SentimentAnalysis.SentimentCategory getSentimentCategory() {
        return sentimentCategory;
    }
    
    public void setSentimentCategory(SentimentAnalysis.SentimentCategory sentimentCategory) {
        this.sentimentCategory = sentimentCategory;
    }
    
    public Double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public String getEmotionalIndicators() {
        return emotionalIndicators;
    }
    
    public void setEmotionalIndicators(String emotionalIndicators) {
        this.emotionalIndicators = emotionalIndicators;
    }
    
    public String getConcernLevel() {
        return concernLevel;
    }
    
    public void setConcernLevel(String concernLevel) {
        this.concernLevel = concernLevel;
    }
    
    public String getReasoning() {
        return reasoning;
    }
    
    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Boolean getRequiresAttention() {
        return requiresAttention;
    }
    
    public void setRequiresAttention(Boolean requiresAttention) {
        this.requiresAttention = requiresAttention;
    }
}