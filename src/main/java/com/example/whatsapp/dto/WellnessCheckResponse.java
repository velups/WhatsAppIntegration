package com.example.whatsapp.dto;

import java.time.LocalDateTime;

public class WellnessCheckResponse {
    
    private String status;
    private String message;
    private String recipientName;
    private String recipientPhoneNumber;
    private String sentMessage;
    private LocalDateTime sentAt;
    private String messageId;
    
    public WellnessCheckResponse() {}
    
    public WellnessCheckResponse(String status, String message, String recipientName, String recipientPhoneNumber, String sentMessage, LocalDateTime sentAt, String messageId) {
        this.status = status;
        this.message = message;
        this.recipientName = recipientName;
        this.recipientPhoneNumber = recipientPhoneNumber;
        this.sentMessage = sentMessage;
        this.sentAt = sentAt;
        this.messageId = messageId;
    }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getRecipientPhoneNumber() { return recipientPhoneNumber; }
    public void setRecipientPhoneNumber(String recipientPhoneNumber) { this.recipientPhoneNumber = recipientPhoneNumber; }
    
    public String getSentMessage() { return sentMessage; }
    public void setSentMessage(String sentMessage) { this.sentMessage = sentMessage; }
    
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
    
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public static WellnessCheckResponseBuilder builder() {
        return new WellnessCheckResponseBuilder();
    }
    
    public static class WellnessCheckResponseBuilder {
        private String status;
        private String message;
        private String recipientName;
        private String recipientPhoneNumber;
        private String sentMessage;
        private LocalDateTime sentAt;
        private String messageId;
        
        public WellnessCheckResponseBuilder status(String status) {
            this.status = status;
            return this;
        }
        
        public WellnessCheckResponseBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public WellnessCheckResponseBuilder recipientName(String recipientName) {
            this.recipientName = recipientName;
            return this;
        }
        
        public WellnessCheckResponseBuilder recipientPhoneNumber(String recipientPhoneNumber) {
            this.recipientPhoneNumber = recipientPhoneNumber;
            return this;
        }
        
        public WellnessCheckResponseBuilder sentMessage(String sentMessage) {
            this.sentMessage = sentMessage;
            return this;
        }
        
        public WellnessCheckResponseBuilder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }
        
        public WellnessCheckResponseBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }
        
        public WellnessCheckResponse build() {
            return new WellnessCheckResponse(status, message, recipientName, recipientPhoneNumber, sentMessage, sentAt, messageId);
        }
    }
    
    public static WellnessCheckResponse success(String recipientName, String phoneNumber, String sentMessage, String messageId) {
        return WellnessCheckResponse.builder()
                .status("success")
                .message("Wellness check message sent successfully")
                .recipientName(recipientName)
                .recipientPhoneNumber(phoneNumber)
                .sentMessage(sentMessage)
                .sentAt(LocalDateTime.now())
                .messageId(messageId)
                .build();
    }
    
    public static WellnessCheckResponse error(String message, String recipientName, String phoneNumber) {
        return WellnessCheckResponse.builder()
                .status("error")
                .message(message)
                .recipientName(recipientName)
                .recipientPhoneNumber(phoneNumber)
                .sentAt(LocalDateTime.now())
                .build();
    }
}