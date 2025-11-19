package com.example.whatsapp.service;

import com.example.whatsapp.dto.SentimentAnalysis;
import com.example.whatsapp.entity.ConversationSentiment;
import com.example.whatsapp.model.Recipient;
import com.example.whatsapp.repository.ConversationSentimentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationSentimentService {

    private static final Logger log = LoggerFactory.getLogger(ConversationSentimentService.class);
    private static final int ALERT_THRESHOLD = 3; // Number of concerning sentiments before alerting caretaker

    @Value("${whatsapp.api.access-token}")
    private String accessToken;

    @Value("${whatsapp.api.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.api.base-url:https://graph.facebook.com/v18.0}")
    private String whatsAppApiBaseUrl;

    private final ConversationSentimentRepository sentimentRepository;
    private final SentimentAnalysisService sentimentAnalysisService;
    private final RecipientService recipientService;
    private final WebClient.Builder webClientBuilder;

    // Track consecutive concerning sentiment counts per user
    private final Map<String, Integer> concerningSentimentCounts = new ConcurrentHashMap<>();
    // Track last alert sent to avoid spamming caretakers
    private final Map<String, LocalDateTime> lastAlertSent = new ConcurrentHashMap<>();

    public ConversationSentimentService(ConversationSentimentRepository sentimentRepository,
                                      SentimentAnalysisService sentimentAnalysisService,
                                      RecipientService recipientService,
                                      WebClient.Builder webClientBuilder) {
        this.sentimentRepository = sentimentRepository;
        this.sentimentAnalysisService = sentimentAnalysisService;
        this.recipientService = recipientService;
        this.webClientBuilder = webClientBuilder;
    }
    
    /**
     * Analyze and store sentiment for a conversation
     */
    public ConversationSentiment analyzeAndStoreSentiment(String phoneNumber, 
                                                        String userMessage, 
                                                        String aiResponse) {
        try {
            // Perform sentiment analysis
            SentimentAnalysis analysis = sentimentAnalysisService.analyzeSentiment(userMessage);
            
            // Create and save sentiment record
            ConversationSentiment sentiment = new ConversationSentiment(
                    phoneNumber, userMessage, aiResponse, analysis);
            
            ConversationSentiment saved = sentimentRepository.save(sentiment);
            
            // Log important sentiment changes and track concerning patterns
            if (analysis.getCategory() == SentimentAnalysis.SentimentCategory.RED ||
                analysis.getCategory() == SentimentAnalysis.SentimentCategory.AMBER) {

                // Increment concerning sentiment count
                int count = concerningSentimentCounts.getOrDefault(phoneNumber, 0) + 1;
                concerningSentimentCounts.put(phoneNumber, count);

                if (analysis.getCategory() == SentimentAnalysis.SentimentCategory.RED) {
                    log.warn("RED ALERT: User {} shows concerning sentiment: {} (confidence: {}). Consecutive count: {}",
                            phoneNumber, analysis.getEmotionalIndicators(), analysis.getConfidence(), count);
                } else {
                    log.info("AMBER: User {} shows neutral/mixed sentiment. Consecutive count: {}", phoneNumber, count);
                }

                // Check if we need to alert caretaker
                if (count >= ALERT_THRESHOLD) {
                    checkAndAlertCaretaker(phoneNumber, analysis, count);
                }

            } else if (analysis.getCategory() == SentimentAnalysis.SentimentCategory.GREEN) {
                // Reset concerning sentiment count on positive sentiment
                concerningSentimentCounts.put(phoneNumber, 0);
                log.info("POSITIVE: User {} shows positive sentiment: {} (confidence: {}). Resetting concern count.",
                        phoneNumber, analysis.getEmotionalIndicators(), analysis.getConfidence());
            }

            return saved;
            
        } catch (Exception e) {
            log.error("Failed to analyze sentiment for user {}: {}", phoneNumber, e.getMessage());
            // Still save a basic record without sentiment analysis
            ConversationSentiment basicSentiment = new ConversationSentiment();
            basicSentiment.setPhoneNumber(phoneNumber);
            basicSentiment.setUserMessage(userMessage);
            basicSentiment.setAiResponse(aiResponse);
            basicSentiment.setSentimentCategory(SentimentAnalysis.SentimentCategory.AMBER);
            basicSentiment.setConfidenceScore(0.0);
            basicSentiment.setReasoning("Analysis failed - manual review recommended");
            return sentimentRepository.save(basicSentiment);
        }
    }
    
    /**
     * Get sentiment history for a user
     */
    public List<ConversationSentiment> getUserSentimentHistory(String phoneNumber) {
        return sentimentRepository.findByPhoneNumberOrderByTimestampDesc(phoneNumber);
    }
    
    /**
     * Get recent sentiment history for a user (last 24 hours)
     */
    public List<ConversationSentiment> getRecentUserSentiments(String phoneNumber) {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        return sentimentRepository.findByPhoneNumberAndTimestampAfterOrderByTimestampDesc(
                phoneNumber, yesterday);
    }
    
    /**
     * Get all conversations requiring attention (RED category)
     */
    public List<ConversationSentiment> getConversationsRequiringAttention() {
        return sentimentRepository.findByRequiresAttentionTrueOrderByTimestampDesc();
    }
    
    /**
     * Get sentiment overview for the last 24 hours
     */
    public Map<String, Object> getSentimentOverview() {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        
        Map<String, Object> overview = new HashMap<>();
        
        // Get recent sentiments
        List<ConversationSentiment> recentSentiments = sentimentRepository.findRecentSentiments(yesterday);
        overview.put("total_conversations", recentSentiments.size());
        
        // Count by category
        List<Object[]> sentimentCounts = sentimentRepository.getSentimentCounts(yesterday);
        Map<String, Long> categoryCounts = new HashMap<>();
        categoryCounts.put("GREEN", 0L);
        categoryCounts.put("AMBER", 0L);
        categoryCounts.put("RED", 0L);
        
        for (Object[] count : sentimentCounts) {
            String category = count[0].toString();
            Long num = (Long) count[1];
            categoryCounts.put(category, num);
        }
        
        overview.put("sentiment_counts", categoryCounts);
        
        // Users requiring attention
        List<String> usersNeedingAttention = sentimentRepository.findUsersWithRecentRedSentiments(yesterday);
        overview.put("users_needing_attention", usersNeedingAttention);
        overview.put("attention_count", usersNeedingAttention.size());
        
        // Calculate percentages
        long total = recentSentiments.size();
        if (total > 0) {
            Map<String, Double> percentages = new HashMap<>();
            percentages.put("green_percentage", (categoryCounts.get("GREEN") * 100.0) / total);
            percentages.put("amber_percentage", (categoryCounts.get("AMBER") * 100.0) / total);
            percentages.put("red_percentage", (categoryCounts.get("RED") * 100.0) / total);
            overview.put("percentages", percentages);
        }
        
        return overview;
    }
    
    /**
     * Get sentiment trends for a specific user
     */
    public Map<String, Object> getUserSentimentTrend(String phoneNumber) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<ConversationSentiment> sentiments = sentimentRepository.findUserSentimentsAfter(
                phoneNumber, weekAgo);
        
        Map<String, Object> trend = new HashMap<>();
        trend.put("total_messages", sentiments.size());
        
        if (!sentiments.isEmpty()) {
            // Latest sentiment
            ConversationSentiment latest = sentiments.get(0);
            trend.put("current_sentiment", latest.getSentimentCategory());
            trend.put("latest_confidence", latest.getConfidenceScore());
            trend.put("latest_indicators", latest.getEmotionalIndicators());
            
            // Count categories in last week
            Map<String, Long> weekCounts = new HashMap<>();
            weekCounts.put("GREEN", 0L);
            weekCounts.put("AMBER", 0L);
            weekCounts.put("RED", 0L);
            
            for (ConversationSentiment s : sentiments) {
                String category = s.getSentimentCategory().toString();
                weekCounts.put(category, weekCounts.get(category) + 1);
            }
            
            trend.put("week_sentiment_counts", weekCounts);
            
            // Check for concerning patterns
            long redCount = weekCounts.get("RED");
            if (redCount > 0) {
                trend.put("concern_alert", true);
                trend.put("red_conversations_count", redCount);
            } else {
                trend.put("concern_alert", false);
            }
        }
        
        return trend;
    }

    /**
     * Check if caretaker should be alerted and send WhatsApp message
     */
    private void checkAndAlertCaretaker(String phoneNumber, SentimentAnalysis analysis, int consecutiveCount) {
        try {
            // Look up recipient to get caretaker info
            Optional<Recipient> recipientOpt = recipientService.getRecipientByPhoneNumber(phoneNumber);

            if (recipientOpt.isEmpty()) {
                log.warn("No recipient found for phone number: {}. Cannot alert caretaker.", phoneNumber);
                return;
            }

            Recipient recipient = recipientOpt.get();
            String caretakerPhone = recipient.getCaretakerPhoneNumber();
            String caretakerName = recipient.getCaretakerName();

            if (caretakerPhone == null || caretakerPhone.isEmpty()) {
                log.warn("No caretaker configured for user {}. Skipping alert.", recipient.getName());
                return;
            }

            // Check if we recently sent an alert (within last hour) to avoid spamming
            LocalDateTime lastAlert = lastAlertSent.get(phoneNumber);
            if (lastAlert != null && lastAlert.isAfter(LocalDateTime.now().minusHours(1))) {
                log.info("Alert already sent to caretaker within last hour for user {}. Skipping.", recipient.getName());
                return;
            }

            // Build alert message
            String userName = recipient.getDisplayName();
            String category = analysis.getCategory().toString();
            String indicators = analysis.getEmotionalIndicators() != null
                    ? String.join(", ", analysis.getEmotionalIndicators())
                    : "not specified";

            String alertMessage = String.format(
                    "⚠️ WELLNESS ALERT ⚠️\n\n" +
                    "Hi %s,\n\n" +
                    "This is an automated alert regarding %s.\n\n" +
                    "📊 Status: %s sentiment detected\n" +
                    "🔢 Consecutive concerning messages: %d\n" +
                    "💭 Emotional indicators: %s\n\n" +
                    "We recommend reaching out to check on their well-being.\n\n" +
                    "- Wellness Companion System",
                    caretakerName != null ? caretakerName : "Caretaker",
                    userName,
                    category,
                    consecutiveCount,
                    indicators
            );

            // Send WhatsApp message to caretaker
            sendCaretakerAlert(caretakerPhone, alertMessage);

            // Update last alert time
            lastAlertSent.put(phoneNumber, LocalDateTime.now());

            log.info("Caretaker alert sent to {} ({}) for user {} after {} concerning messages",
                    caretakerName, caretakerPhone, userName, consecutiveCount);

        } catch (Exception e) {
            log.error("Failed to send caretaker alert for user {}: {}", phoneNumber, e.getMessage());
        }
    }

    /**
     * Send WhatsApp message to caretaker
     */
    private void sendCaretakerAlert(String caretakerPhoneNumber, String messageText) {
        try {
            WebClient webClient = webClientBuilder
                    .baseUrl(whatsAppApiBaseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            // Build the message payload
            Map<String, Object> message = new HashMap<>();
            message.put("messaging_product", "whatsapp");
            message.put("recipient_type", "individual");
            message.put("to", caretakerPhoneNumber.replaceAll("^\\+", ""));

            Map<String, String> text = new HashMap<>();
            text.put("preview_url", "false");
            text.put("body", messageText);
            message.put("type", "text");
            message.put("text", text);

            String response = webClient
                    .post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .body(Mono.just(message), Map.class)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully sent caretaker alert to {}. Response: {}", caretakerPhoneNumber, response);

        } catch (Exception e) {
            log.error("Error sending caretaker alert to {}: {}", caretakerPhoneNumber, e.getMessage());
        }
    }

    /**
     * Get current concerning sentiment count for a user
     */
    public int getConcerningSentimentCount(String phoneNumber) {
        return concerningSentimentCounts.getOrDefault(phoneNumber, 0);
    }

    /**
     * Reset concerning sentiment count for a user (e.g., after manual intervention)
     */
    public void resetConcerningSentimentCount(String phoneNumber) {
        concerningSentimentCounts.put(phoneNumber, 0);
        log.info("Reset concerning sentiment count for user: {}", phoneNumber);
    }
}