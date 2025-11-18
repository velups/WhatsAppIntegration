package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GroqResponse {
    
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    
    // Constructors
    public GroqResponse() {
    }
    
    public GroqResponse(String id, String object, long created, String model, List<Choice> choices, Usage usage) {
        this.id = id;
        this.object = object;
        this.created = created;
        this.model = model;
        this.choices = choices;
        this.usage = usage;
    }
    
    // Getters
    public String getId() {
        return id;
    }
    
    public String getObject() {
        return object;
    }
    
    public long getCreated() {
        return created;
    }
    
    public String getModel() {
        return model;
    }
    
    public List<Choice> getChoices() {
        return choices;
    }
    
    public Usage getUsage() {
        return usage;
    }
    
    // Setters
    public void setId(String id) {
        this.id = id;
    }
    
    public void setObject(String object) {
        this.object = object;
    }
    
    public void setCreated(long created) {
        this.created = created;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public void setChoices(List<Choice> choices) {
        this.choices = choices;
    }
    
    public void setUsage(Usage usage) {
        this.usage = usage;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        private int index;
        private Message message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
        
        // Constructors
        public Choice() {
        }
        
        public Choice(int index, Message message, String finishReason) {
            this.index = index;
            this.message = message;
            this.finishReason = finishReason;
        }
        
        // Getters
        public int getIndex() {
            return index;
        }
        
        public Message getMessage() {
            return message;
        }
        
        public String getFinishReason() {
            return finishReason;
        }
        
        // Setters
        public void setIndex(int index) {
            this.index = index;
        }
        
        public void setMessage(Message message) {
            this.message = message;
        }
        
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String role;
        private String content;
        
        // Constructors
        public Message() {
        }
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        // Getters
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
        
        // Setters
        public void setRole(String role) {
            this.role = role;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        @JsonProperty("completion_tokens")
        private int completionTokens;
        
        @JsonProperty("total_tokens")
        private int totalTokens;
        
        // Constructors
        public Usage() {
        }
        
        public Usage(int promptTokens, int completionTokens, int totalTokens) {
            this.promptTokens = promptTokens;
            this.completionTokens = completionTokens;
            this.totalTokens = totalTokens;
        }
        
        // Getters
        public int getPromptTokens() {
            return promptTokens;
        }
        
        public int getCompletionTokens() {
            return completionTokens;
        }
        
        public int getTotalTokens() {
            return totalTokens;
        }
        
        // Setters
        public void setPromptTokens(int promptTokens) {
            this.promptTokens = promptTokens;
        }
        
        public void setCompletionTokens(int completionTokens) {
            this.completionTokens = completionTokens;
        }
        
        public void setTotalTokens(int totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}