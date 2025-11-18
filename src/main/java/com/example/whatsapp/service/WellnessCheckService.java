package com.example.whatsapp.service;

import com.example.whatsapp.dto.WellnessCheckRequest;
import com.example.whatsapp.dto.WellnessCheckResponse;
import com.example.whatsapp.dto.WhatsAppOutgoingMessage;
import com.example.whatsapp.model.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Service
public class WellnessCheckService {
    
    private static final Logger log = LoggerFactory.getLogger(WellnessCheckService.class);
    
    @Value("${whatsapp.api.access-token}")
    private String accessToken;
    
    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;
    
    @Value("${whatsapp.api.base-url:https://graph.facebook.com/v18.0}")
    private String whatsAppApiBaseUrl;
    
    @Value("${wellness.scheduler.enabled:true}")
    private boolean schedulerEnabled;
    
    @Value("${wellness.scheduler.morning-hour:9}")
    private int morningHour;
    
    @Value("${wellness.scheduler.afternoon-hour:14}")
    private int afternoonHour;
    
    @Value("${wellness.scheduler.evening-hour:19}")
    private int eveningHour;
    
    private final WebClient.Builder webClientBuilder;
    private final RecipientService recipientService;
    private final GroqService groqService;
    
    public WellnessCheckService(WebClient.Builder webClientBuilder, RecipientService recipientService, GroqService groqService) {
        this.webClientBuilder = webClientBuilder;
        this.recipientService = recipientService;
        this.groqService = groqService;
    }
    
    private static final String[] MORNING_GREETINGS = {
        "Good morning {name}! 🌅 Hope you slept well. How are you feeling today?",
        "Morning {name}! ☀️ Wishing you a wonderful day ahead. How are you doing?",
        "Hello {name}! 🌞 Hope you're having a lovely morning. How are you today?",
        "Good morning dear {name}! 🌸 Just checking in to see how you're doing."
    };
    
    private static final String[] AFTERNOON_GREETINGS = {
        "Good afternoon {name}! 🌤️ Hope your day is going well. How are you feeling?",
        "Hello {name}! 😊 Just wanted to check in this afternoon. How has your day been?",
        "Hi {name}! 🌻 Hope you're having a pleasant afternoon. How are you doing?",
        "Good afternoon dear {name}! 💛 Thinking of you today. How are things?"
    };
    
    private static final String[] EVENING_GREETINGS = {
        "Good evening {name}! 🌙 Hope you had a wonderful day. How are you feeling?",
        "Evening {name}! ⭐ Just checking in to see how your day went. How are you?",
        "Hello {name}! 🌆 Hope your evening is peaceful. How has your day been?",
        "Good evening dear {name}! 🕯️ Wishing you a restful evening. How are you doing?"
    };
    
    /**
     * Send wellness check message manually
     */
    public WellnessCheckResponse sendWellnessCheck(WellnessCheckRequest request) {
        try {
            String message = generateWellnessMessage(request.getName(), 
                    request.getCustomMessage(), request.getTimeOfDay());
            
            String messageId = sendWhatsAppMessage(request.getPhoneNumber(), message);
            
            // Update last check sent time if this is a known recipient
            recipientService.updateLastCheckSent(request.getPhoneNumber());
            
            return WellnessCheckResponse.success(
                    request.getName(), 
                    request.getPhoneNumber(), 
                    message, 
                    messageId
            );
            
        } catch (Exception e) {
            log.error("Error sending wellness check to {} ({})", request.getName(), request.getPhoneNumber(), e);
            return WellnessCheckResponse.error(
                    "Failed to send wellness check: " + e.getMessage(),
                    request.getName(),
                    request.getPhoneNumber()
            );
        }
    }
    
    /**
     * Scheduled wellness checks - runs every hour to check if any messages need to be sent
     */
    @Scheduled(cron = "0 0 */1 * * *") // Every hour
    public void scheduledWellnessCheck() {
        if (!schedulerEnabled) {
            log.debug("Wellness check scheduler is disabled");
            return;
        }
        
        LocalTime now = LocalTime.now();
        int currentHour = now.getHour();
        
        // Determine time of day
        String timeOfDay = getTimeOfDay(currentHour);
        if (timeOfDay == null) {
            log.debug("Current time ({}) not in wellness check hours", currentHour);
            return;
        }
        
        List<Recipient> recipientsDue = recipientService.getRecipientsDueForCheck();
        if (recipientsDue.isEmpty()) {
            log.debug("No recipients due for wellness check at {}", currentHour);
            return;
        }
        
        log.info("Starting {} wellness checks at {} ({})", recipientsDue.size(), currentHour, timeOfDay);
        
        for (Recipient recipient : recipientsDue) {
            try {
                // Check if this is the right time for this recipient
                if (shouldSendAtThisTime(recipient, timeOfDay)) {
                    String message = generateWellnessMessage(
                            recipient.getName(), 
                            recipient.getCustomMessage(), 
                            timeOfDay
                    );
                    
                    String messageId = sendWhatsAppMessage(recipient.getPhoneNumber(), message);
                    recipientService.updateLastCheckSent(recipient.getPhoneNumber());
                    
                    log.info("Sent wellness check to {} ({}): {}", 
                            recipient.getName(), recipient.getPhoneNumber(), messageId);
                    
                    // Add delay between messages to avoid rate limiting
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                log.error("Failed to send wellness check to {} ({})", 
                        recipient.getName(), recipient.getPhoneNumber(), e);
            }
        }
    }
    
    private String getTimeOfDay(int hour) {
        if (hour >= morningHour && hour < afternoonHour) {
            return "morning";
        } else if (hour >= afternoonHour && hour < eveningHour) {
            return "afternoon";
        } else if (hour >= eveningHour && hour <= 21) { // Until 9 PM
            return "evening";
        }
        return null; // Outside wellness check hours
    }
    
    private boolean shouldSendAtThisTime(Recipient recipient, String currentTimeOfDay) {
        String preferredTime = recipient.getPreferredTimeOfDay();
        return preferredTime == null || preferredTime.isEmpty() || preferredTime.equals(currentTimeOfDay);
    }
    
    private String generateWellnessMessage(String name, String customMessage, String timeOfDay) {
        if (customMessage != null && !customMessage.isEmpty()) {
            return customMessage.replace("{name}", name);
        }
        
        // Use predefined messages based on time of day
        String[] messages;
        switch (timeOfDay != null ? timeOfDay.toLowerCase() : "morning") {
            case "afternoon":
                messages = AFTERNOON_GREETINGS;
                break;
            case "evening":
                messages = EVENING_GREETINGS;
                break;
            default:
                messages = MORNING_GREETINGS;
                break;
        }
        
        // Randomly select a message
        String template = messages[new Random().nextInt(messages.length)];
        return template.replace("{name}", name != null ? name : "friend");
    }
    
    private String sendWhatsAppMessage(String recipientPhoneNumber, String messageText) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(whatsAppApiBaseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            
            WhatsAppOutgoingMessage outgoingMessage = WhatsAppOutgoingMessage.createTextMessage(
                    recipientPhoneNumber, 
                    messageText
            );
            
            String response = webClient
                    .post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(Mono.just(outgoingMessage), WhatsAppOutgoingMessage.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            log.debug("WhatsApp API response: {}", response);
            return "wellness_" + System.currentTimeMillis();
            
        } catch (Exception e) {
            log.error("Error sending WhatsApp wellness check message to {}", recipientPhoneNumber, e);
            throw new RuntimeException("Failed to send WhatsApp message", e);
        }
    }
    
    /**
     * Get wellness check statistics
     */
    public WellnessCheckStats getStats() {
        List<Recipient> allRecipients = recipientService.getAllRecipients();
        List<Recipient> enabledRecipients = recipientService.getEnabledRecipients();
        List<Recipient> recipientsDue = recipientService.getRecipientsDueForCheck();
        
        return WellnessCheckStats.builder()
                .totalRecipients(allRecipients.size())
                .enabledRecipients(enabledRecipients.size())
                .recipientsDueForCheck(recipientsDue.size())
                .schedulerEnabled(schedulerEnabled)
                .nextCheckHours(List.of(morningHour, afternoonHour, eveningHour))
                .build();
    }
    
    public static class WellnessCheckStats {
        private int totalRecipients;
        private int enabledRecipients;
        private int recipientsDueForCheck;
        private boolean schedulerEnabled;
        private List<Integer> nextCheckHours;
        
        // Constructors
        public WellnessCheckStats() {
        }
        
        public WellnessCheckStats(int totalRecipients, int enabledRecipients, int recipientsDueForCheck, boolean schedulerEnabled, List<Integer> nextCheckHours) {
            this.totalRecipients = totalRecipients;
            this.enabledRecipients = enabledRecipients;
            this.recipientsDueForCheck = recipientsDueForCheck;
            this.schedulerEnabled = schedulerEnabled;
            this.nextCheckHours = nextCheckHours;
        }
        
        // Getters
        public int getTotalRecipients() { return totalRecipients; }
        public int getEnabledRecipients() { return enabledRecipients; }
        public int getRecipientsDueForCheck() { return recipientsDueForCheck; }
        public boolean isSchedulerEnabled() { return schedulerEnabled; }
        public List<Integer> getNextCheckHours() { return nextCheckHours; }
        
        // Setters
        public void setTotalRecipients(int totalRecipients) { this.totalRecipients = totalRecipients; }
        public void setEnabledRecipients(int enabledRecipients) { this.enabledRecipients = enabledRecipients; }
        public void setRecipientsDueForCheck(int recipientsDueForCheck) { this.recipientsDueForCheck = recipientsDueForCheck; }
        public void setSchedulerEnabled(boolean schedulerEnabled) { this.schedulerEnabled = schedulerEnabled; }
        public void setNextCheckHours(List<Integer> nextCheckHours) { this.nextCheckHours = nextCheckHours; }
        
        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private int totalRecipients;
            private int enabledRecipients;
            private int recipientsDueForCheck;
            private boolean schedulerEnabled;
            private List<Integer> nextCheckHours;
            
            public Builder totalRecipients(int totalRecipients) {
                this.totalRecipients = totalRecipients;
                return this;
            }
            
            public Builder enabledRecipients(int enabledRecipients) {
                this.enabledRecipients = enabledRecipients;
                return this;
            }
            
            public Builder recipientsDueForCheck(int recipientsDueForCheck) {
                this.recipientsDueForCheck = recipientsDueForCheck;
                return this;
            }
            
            public Builder schedulerEnabled(boolean schedulerEnabled) {
                this.schedulerEnabled = schedulerEnabled;
                return this;
            }
            
            public Builder nextCheckHours(List<Integer> nextCheckHours) {
                this.nextCheckHours = nextCheckHours;
                return this;
            }
            
            public WellnessCheckStats build() {
                return new WellnessCheckStats(totalRecipients, enabledRecipients, recipientsDueForCheck, schedulerEnabled, nextCheckHours);
            }
        }
    }
}