package com.example.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class WellnessCheckRequest {
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @NotBlank(message = "Recipient name is required")
    private String name;
    
    private String customMessage;
    
    private String timeOfDay; // morning, afternoon, evening
    
    // Constructors
    public WellnessCheckRequest() {
    }
    
    public WellnessCheckRequest(String phoneNumber, String name, String customMessage, String timeOfDay) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.customMessage = customMessage;
        this.timeOfDay = timeOfDay;
    }
    
    // Getters
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCustomMessage() {
        return customMessage;
    }
    
    public String getTimeOfDay() {
        return timeOfDay;
    }
    
    // Setters
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }
    
    public void setTimeOfDay(String timeOfDay) {
        this.timeOfDay = timeOfDay;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String phoneNumber;
        private String name;
        private String customMessage;
        private String timeOfDay;
        
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder customMessage(String customMessage) {
            this.customMessage = customMessage;
            return this;
        }
        
        public Builder timeOfDay(String timeOfDay) {
            this.timeOfDay = timeOfDay;
            return this;
        }
        
        public WellnessCheckRequest build() {
            return new WellnessCheckRequest(phoneNumber, name, customMessage, timeOfDay);
        }
    }
}