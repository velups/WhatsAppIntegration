package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SentimentAnalysis {
    
    public enum SentimentCategory {
        GREEN,   // Positive, happy, content
        AMBER,   // Neutral, mild concerns, mixed emotions
        RED      // Negative, sad, distressed, needs attention
    }
    
    @JsonProperty("category")
    private SentimentCategory category;
    
    @JsonProperty("confidence")
    private Double confidence; // 0.0 to 1.0
    
    @JsonProperty("emotional_indicators")
    private String emotionalIndicators;
    
    @JsonProperty("concern_level")
    private String concernLevel;
    
    @JsonProperty("reasoning")
    private String reasoning;
    
    public SentimentAnalysis() {}
    
    public SentimentAnalysis(SentimentCategory category, Double confidence, 
                           String emotionalIndicators, String concernLevel, String reasoning) {
        this.category = category;
        this.confidence = confidence;
        this.emotionalIndicators = emotionalIndicators;
        this.concernLevel = concernLevel;
        this.reasoning = reasoning;
    }
    
    public static SentimentAnalysis green(Double confidence, String indicators, String reasoning) {
        return new SentimentAnalysis(SentimentCategory.GREEN, confidence, indicators, "Low", reasoning);
    }
    
    public static SentimentAnalysis amber(Double confidence, String indicators, String reasoning) {
        return new SentimentAnalysis(SentimentCategory.AMBER, confidence, indicators, "Medium", reasoning);
    }
    
    public static SentimentAnalysis red(Double confidence, String indicators, String reasoning) {
        return new SentimentAnalysis(SentimentCategory.RED, confidence, indicators, "High", reasoning);
    }
    
    // Getters and Setters
    public SentimentCategory getCategory() {
        return category;
    }
    
    public void setCategory(SentimentCategory category) {
        this.category = category;
    }
    
    public Double getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Double confidence) {
        this.confidence = confidence;
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
}