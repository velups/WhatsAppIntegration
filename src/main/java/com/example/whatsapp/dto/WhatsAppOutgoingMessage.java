package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppOutgoingMessage {
    
    @JsonProperty("messaging_product")
    private String messagingProduct = "whatsapp";
    
    @JsonProperty("recipient_type")
    private String recipientType = "individual";
    
    private String to;
    
    private String type = "text";
    
    private TextMessage text;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TextMessage {
        private String body;
        
        @JsonProperty("preview_url")
        private boolean previewUrl;
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