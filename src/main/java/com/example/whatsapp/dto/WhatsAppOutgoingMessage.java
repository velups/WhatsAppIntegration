package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WhatsAppOutgoingMessage {
    
    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";
    
    @JsonProperty("recipient_type")
    private String recipientType = "individual";
    
    private String to;
    
    private String type = "text";
    
    private TextMessage text;
    
    public WhatsAppOutgoingMessage() {}
    
    public WhatsAppOutgoingMessage(String messagingProduct, String recipientType, String to, String type, TextMessage text) {
        this.messagingProduct = messagingProduct;
        this.recipientType = recipientType;
        this.to = to;
        this.type = type;
        this.text = text;
    }
    
    public String getMessagingProduct() { return messagingProduct; }
    public void setMessagingProduct(String messagingProduct) { this.messagingProduct = messagingProduct; }
    
    public String getRecipientType() { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }
    
    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public TextMessage getText() { return text; }
    public void setText(TextMessage text) { this.text = text; }
    
    public static WhatsAppOutgoingMessageBuilder builder() {
        return new WhatsAppOutgoingMessageBuilder();
    }
    
    public static class WhatsAppOutgoingMessageBuilder {
        private String messagingProduct = "whatsapp";
        private String recipientType = "individual";
        private String to;
        private String type = "text";
        private TextMessage text;
        
        public WhatsAppOutgoingMessageBuilder messagingProduct(String messagingProduct) {
            this.messagingProduct = messagingProduct;
            return this;
        }
        
        public WhatsAppOutgoingMessageBuilder recipientType(String recipientType) {
            this.recipientType = recipientType;
            return this;
        }
        
        public WhatsAppOutgoingMessageBuilder to(String to) {
            this.to = to;
            return this;
        }
        
        public WhatsAppOutgoingMessageBuilder type(String type) {
            this.type = type;
            return this;
        }
        
        public WhatsAppOutgoingMessageBuilder text(TextMessage text) {
            this.text = text;
            return this;
        }
        
        public WhatsAppOutgoingMessage build() {
            return new WhatsAppOutgoingMessage(messagingProduct, recipientType, to, type, text);
        }
    }
    
    public static class TextMessage {
        private String body;
        
        @JsonProperty("preview_url")
        private boolean previewUrl;
        
        public TextMessage() {}
        
        public TextMessage(String body, boolean previewUrl) {
            this.body = body;
            this.previewUrl = previewUrl;
        }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        
        public boolean isPreviewUrl() { return previewUrl; }
        public void setPreviewUrl(boolean previewUrl) { this.previewUrl = previewUrl; }
        
        public static TextMessageBuilder builder() {
            return new TextMessageBuilder();
        }
        
        public static class TextMessageBuilder {
            private String body;
            private boolean previewUrl;
            
            public TextMessageBuilder body(String body) {
                this.body = body;
                return this;
            }
            
            public TextMessageBuilder previewUrl(boolean previewUrl) {
                this.previewUrl = previewUrl;
                return this;
            }
            
            public TextMessage build() {
                return new TextMessage(body, previewUrl);
            }
        }
    }
    
    public static WhatsAppOutgoingMessage createTextMessage(String to, String message) {
        return WhatsAppOutgoingMessage.builder()
                .messagingProduct("whatsapp")
                .recipientType("individual")
                .to(to)
                .type("text")
                .text(TextMessage.builder()
                        .body(message)
                        .previewUrl(false)
                        .build())
                .build();
    }
}