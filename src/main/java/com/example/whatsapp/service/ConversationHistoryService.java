package com.example.whatsapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationHistoryService {
    
    private static final Logger log = LoggerFactory.getLogger(ConversationHistoryService.class);
    
    @Value("${conversation.history.max-messages:20}")
    private int maxMessagesPerConversation;
    
    @Value("${conversation.history.ttl-hours:24}")
    private int conversationTtlHours;
    
    // In-memory storage for conversation history (in production, consider using Redis or a database)
    private final Map<String, ConversationContext> conversations = new ConcurrentHashMap<>();
    
    /**
     * Simple chat message class for Groq compatibility
     */
    public static class ChatMessage {
        private final String role;
        private final String content;
        
        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
        
        public String getRole() {
            return role;
        }
        
        public String getContent() {
            return content;
        }
    }
    
    /**
     * Conversation context for each user
     */
    public static class ConversationContext {
        private final String phoneNumber;
        private final List<ChatMessage> messages;
        private LocalDateTime lastActivity;
        private boolean isFirstMessage;
        private String userName;
        
        public ConversationContext(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            this.messages = new ArrayList<>();
            this.lastActivity = LocalDateTime.now();
            this.isFirstMessage = true;
            this.userName = "Aunty"; // Default name
        }
        
        public void addMessage(ChatMessage message) {
            messages.add(message);
            // Keep only the last N messages to manage token limits
            if (messages.size() > 20) {
                messages.remove(0);
            }
            lastActivity = LocalDateTime.now();
        }
        
        public List<ChatMessage> getMessages() {
            return new ArrayList<>(messages);
        }
        
        public boolean isExpired(int ttlHours) {
            return lastActivity.isBefore(LocalDateTime.now().minusHours(ttlHours));
        }
        
        public boolean isFirstMessage() {
            return isFirstMessage;
        }
        
        public void setFirstMessage(boolean firstMessage) {
            isFirstMessage = firstMessage;
        }
        
        public String getUserName() {
            return userName;
        }
        
        public void setUserName(String userName) {
            if (userName != null && !userName.isEmpty()) {
                this.userName = userName;
            }
        }
    }
    
    /**
     * Get or create conversation context for a user
     */
    public ConversationContext getOrCreateContext(String phoneNumber) {
        return conversations.computeIfAbsent(phoneNumber, k -> {
            log.info("Creating new conversation context for: {}", phoneNumber);
            return new ConversationContext(phoneNumber);
        });
    }
    
    /**
     * Add a user message to the conversation history
     */
    public void addUserMessage(String phoneNumber, String message) {
        ConversationContext context = getOrCreateContext(phoneNumber);
        context.addMessage(new ChatMessage("user", message));
        log.debug("Added user message to conversation for {}: {}", phoneNumber, message);
    }
    
    /**
     * Add an assistant message to the conversation history
     */
    public void addAssistantMessage(String phoneNumber, String message) {
        ConversationContext context = getOrCreateContext(phoneNumber);
        context.addMessage(new ChatMessage("assistant", message));
        log.debug("Added assistant message to conversation for {}: {}", phoneNumber, message);
    }
    
    /**
     * Get conversation history for a user
     */
    public List<ChatMessage> getConversationHistory(String phoneNumber) {
        ConversationContext context = conversations.get(phoneNumber);
        if (context == null) {
            return new ArrayList<>();
        }
        return context.getMessages();
    }
    
    /**
     * Check if this is the first message from a user
     */
    public boolean isFirstMessage(String phoneNumber) {
        ConversationContext context = conversations.get(phoneNumber);
        return context == null || context.isFirstMessage();
    }
    
    /**
     * Mark that first message has been processed
     */
    public void markFirstMessageProcessed(String phoneNumber) {
        ConversationContext context = getOrCreateContext(phoneNumber);
        context.setFirstMessage(false);
    }
    
    /**
     * Clear conversation history for a user
     */
    public void clearConversation(String phoneNumber) {
        conversations.remove(phoneNumber);
        log.info("Cleared conversation history for: {}", phoneNumber);
    }
    
    /**
     * Clean up expired conversations (runs every hour)
     */
    @Scheduled(fixedDelay = 3600000) // Run every hour
    public void cleanupExpiredConversations() {
        log.info("Starting cleanup of expired conversations");
        int removedCount = 0;
        
        Iterator<Map.Entry<String, ConversationContext>> iterator = conversations.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ConversationContext> entry = iterator.next();
            if (entry.getValue().isExpired(conversationTtlHours)) {
                iterator.remove();
                removedCount++;
                log.debug("Removed expired conversation for: {}", entry.getKey());
            }
        }
        
        if (removedCount > 0) {
            log.info("Cleanup completed. Removed {} expired conversations", removedCount);
        }
    }
    
    /**
     * Get statistics about active conversations
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeConversations", conversations.size());
        stats.put("totalMessages", conversations.values().stream()
                .mapToInt(c -> c.getMessages().size())
                .sum());
        return stats;
    }
}