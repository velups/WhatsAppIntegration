package com.example.whatsapp.dto;

public class WhatsAppMessageResponse {
    
    private String status;
    private String message;
    private MessageData data;
    
    // Constructors
    public WhatsAppMessageResponse() {
    }
    
    public WhatsAppMessageResponse(String status, String message, MessageData data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    // Getters
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public MessageData getData() {
        return data;
    }
    
    // Setters
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setData(MessageData data) {
        this.data = data;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String status;
        private String message;
        private MessageData data;
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder data(MessageData data) {
            this.data = data;
            return this;
        }
        
        public WhatsAppMessageResponse build() {
            return new WhatsAppMessageResponse(status, message, data);
        }
    }
    
    public static class MessageData {
        private String messageId;
        private String recipientPhoneNumber;
        private String messageContent;
        private String timestamp;
        
        // Constructors
        public MessageData() {
        }
        
        public MessageData(String messageId, String recipientPhoneNumber, String messageContent, String timestamp) {
            this.messageId = messageId;
            this.recipientPhoneNumber = recipientPhoneNumber;
            this.messageContent = messageContent;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getMessageId() {
            return messageId;
        }
        
        public String getRecipientPhoneNumber() {
            return recipientPhoneNumber;
        }
        
        public String getMessageContent() {
            return messageContent;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        // Setters
        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
        
        public void setRecipientPhoneNumber(String recipientPhoneNumber) {
            this.recipientPhoneNumber = recipientPhoneNumber;
        }
        
        public void setMessageContent(String messageContent) {
            this.messageContent = messageContent;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private String messageId;
            private String recipientPhoneNumber;
            private String messageContent;
            private String timestamp;
            
            public Builder messageId(String messageId) {
                this.messageId = messageId;
                return this;
            }
            
            public Builder recipientPhoneNumber(String recipientPhoneNumber) {
                this.recipientPhoneNumber = recipientPhoneNumber;
                return this;
            }
            
            public Builder messageContent(String messageContent) {
                this.messageContent = messageContent;
                return this;
            }
            
            public Builder timestamp(String timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public MessageData build() {
                return new MessageData(messageId, recipientPhoneNumber, messageContent, timestamp);
            }
        }
    }
    
    public static WhatsAppMessageResponse success(String message, MessageData data) {
        return WhatsAppMessageResponse.builder()
                .status("success")
                .message(message)
                .data(data)
                .build();
    }
    
    public static WhatsAppMessageResponse error(String message) {
        return WhatsAppMessageResponse.builder()
                .status("error")
                .message(message)
                .build();
    }
}