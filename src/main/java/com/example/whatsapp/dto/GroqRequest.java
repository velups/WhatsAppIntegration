package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroqRequest {
    
    private List<Message> messages;
    private String model;
    private int max_tokens;
    private double temperature;
    private boolean stream;
    
    // Constructors
    public GroqRequest() {
    }
    
    public GroqRequest(List<Message> messages, String model, int max_tokens, double temperature, boolean stream) {
        this.messages = messages;
        this.model = model;
        this.max_tokens = max_tokens;
        this.temperature = temperature;
        this.stream = stream;
    }
    
    // Getters
    public List<Message> getMessages() {
        return messages;
    }
    
    public String getModel() {
        return model;
    }
    
    public int getMax_tokens() {
        return max_tokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public boolean isStream() {
        return stream;
    }
    
    // Setters
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public void setMax_tokens(int max_tokens) {
        this.max_tokens = max_tokens;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public void setStream(boolean stream) {
        this.stream = stream;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private List<Message> messages;
        private String model;
        private int max_tokens;
        private double temperature;
        private boolean stream;
        
        public Builder messages(List<Message> messages) {
            this.messages = messages;
            return this;
        }
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder max_tokens(int max_tokens) {
            this.max_tokens = max_tokens;
            return this;
        }
        
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder stream(boolean stream) {
            this.stream = stream;
            return this;
        }
        
        public GroqRequest build() {
            return new GroqRequest(messages, model, max_tokens, temperature, stream);
        }
    }
    
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
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String role;
            private String content;
            
            public Builder role(String role) {
                this.role = role;
                return this;
            }
            
            public Builder content(String content) {
                this.content = content;
                return this;
            }
            
            public Message build() {
                return new Message(role, content);
            }
        }
    }
    
    public static GroqRequest create(List<Message> messages, String model) {
        return GroqRequest.builder()
                .messages(messages)
                .model(model)
                .max_tokens(500)
                .temperature(0.8)
                .stream(false)
                .build();
    }
}