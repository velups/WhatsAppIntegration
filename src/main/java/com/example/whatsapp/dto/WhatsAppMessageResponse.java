package com.example.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppMessageResponse {
    
    private String status;
    private String message;
    private MessageData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageData {
        private String messageId;
        private String recipientPhoneNumber;
        private String messageContent;
        private String timestamp;
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