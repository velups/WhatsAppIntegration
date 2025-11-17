package com.example.whatsapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroqRequest {
    
    private List<Message> messages;
    private String model;
    private int max_tokens;
    private double temperature;
    private boolean stream;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Message {
        private String role;
        private String content;
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