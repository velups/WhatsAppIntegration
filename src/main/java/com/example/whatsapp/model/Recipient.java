package com.example.whatsapp.model;

import java.time.LocalDateTime;

public class Recipient {
    
    private String phoneNumber;
    private String name;
    private String preferredTimeOfDay; // morning, afternoon, evening
    private String customMessage;
    private boolean enabled;
    private LocalDateTime lastCheckSent;
    private String timezone; // e.g., "Asia/Singapore", "America/New_York"
    private String relationship; // e.g., "daughter", "son", "friend"
    private String notes;
    
    // Constructors
    public Recipient() {
    }
    
    public Recipient(String phoneNumber, String name, String preferredTimeOfDay, String customMessage, 
                    boolean enabled, LocalDateTime lastCheckSent, String timezone, String relationship, String notes) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.preferredTimeOfDay = preferredTimeOfDay;
        this.customMessage = customMessage;
        this.enabled = enabled;
        this.lastCheckSent = lastCheckSent;
        this.timezone = timezone;
        this.relationship = relationship;
        this.notes = notes;
    }
    
    // Getters
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public String getName() {
        return name;
    }
    
    public String getPreferredTimeOfDay() {
        return preferredTimeOfDay;
    }
    
    public String getCustomMessage() {
        return customMessage;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public LocalDateTime getLastCheckSent() {
        return lastCheckSent;
    }
    
    public String getTimezone() {
        return timezone;
    }
    
    public String getRelationship() {
        return relationship;
    }
    
    public String getNotes() {
        return notes;
    }
    
    // Setters
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setPreferredTimeOfDay(String preferredTimeOfDay) {
        this.preferredTimeOfDay = preferredTimeOfDay;
    }
    
    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setLastCheckSent(LocalDateTime lastCheckSent) {
        this.lastCheckSent = lastCheckSent;
    }
    
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
    
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String phoneNumber;
        private String name;
        private String preferredTimeOfDay;
        private String customMessage;
        private boolean enabled;
        private LocalDateTime lastCheckSent;
        private String timezone;
        private String relationship;
        private String notes;
        
        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder preferredTimeOfDay(String preferredTimeOfDay) {
            this.preferredTimeOfDay = preferredTimeOfDay;
            return this;
        }
        
        public Builder customMessage(String customMessage) {
            this.customMessage = customMessage;
            return this;
        }
        
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder lastCheckSent(LocalDateTime lastCheckSent) {
            this.lastCheckSent = lastCheckSent;
            return this;
        }
        
        public Builder timezone(String timezone) {
            this.timezone = timezone;
            return this;
        }
        
        public Builder relationship(String relationship) {
            this.relationship = relationship;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public Recipient build() {
            return new Recipient(phoneNumber, name, preferredTimeOfDay, customMessage, 
                               enabled, lastCheckSent, timezone, relationship, notes);
        }
    }
    
    public String getDisplayName() {
        return name != null && !name.isEmpty() ? name : "Dear Friend";
    }
    
    public boolean shouldSendCheck() {
        return enabled && (lastCheckSent == null || 
               lastCheckSent.isBefore(LocalDateTime.now().minusHours(20))); // Allow flexibility
    }
}