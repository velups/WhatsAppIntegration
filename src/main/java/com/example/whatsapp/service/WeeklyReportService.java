package com.example.whatsapp.service;

import com.example.whatsapp.dto.SentimentAnalysis;
import com.example.whatsapp.entity.ConversationSentiment;
import com.example.whatsapp.entity.RecipientEntity;
import com.example.whatsapp.entity.RecipientTopicEntity;
import com.example.whatsapp.repository.ConversationSentimentRepository;
import com.example.whatsapp.repository.RecipientRepository;
import com.example.whatsapp.repository.RecipientTopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeeklyReportService {

    private static final Logger logger = LoggerFactory.getLogger(WeeklyReportService.class);

    private final ConversationSentimentRepository sentimentRepository;
    private final RecipientRepository recipientRepository;
    private final RecipientTopicRepository topicRepository;
    private final WhatsAppService whatsAppService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public WeeklyReportService(ConversationSentimentRepository sentimentRepository,
                               RecipientRepository recipientRepository,
                               RecipientTopicRepository topicRepository,
                               WhatsAppService whatsAppService) {
        this.sentimentRepository = sentimentRepository;
        this.recipientRepository = recipientRepository;
        this.topicRepository = topicRepository;
        this.whatsAppService = whatsAppService;
    }

    public Map<String, Object> generateAndSendReport(String phoneNumber) {
        Map<String, Object> result = new HashMap<>();

        Optional<RecipientEntity> recipientOpt = recipientRepository.findByPhoneNumber(phoneNumber);
        if (recipientOpt.isEmpty()) {
            result.put("success", false);
            result.put("error", "Recipient not found: " + phoneNumber);
            return result;
        }

        RecipientEntity recipient = recipientOpt.get();
        Map<String, Object> reportData = generateReportData(phoneNumber, recipient);

        RecipientEntity.ReportPreference preference = recipient.getReportPreference();
        if (preference == null) {
            preference = RecipientEntity.ReportPreference.WHATSAPP;
        }

        boolean emailSent = false;
        boolean whatsAppSent = false;

        if (preference == RecipientEntity.ReportPreference.EMAIL ||
            preference == RecipientEntity.ReportPreference.BOTH) {
            emailSent = sendEmailReport(recipient, reportData);
        }

        if (preference == RecipientEntity.ReportPreference.WHATSAPP ||
            preference == RecipientEntity.ReportPreference.BOTH) {
            whatsAppSent = sendWhatsAppReport(recipient, reportData);
        }

        result.put("success", emailSent || whatsAppSent);
        result.put("email_sent", emailSent);
        result.put("whatsapp_sent", whatsAppSent);
        result.put("report_data", reportData);

        return result;
    }

    public Map<String, Object> generateReportData(String phoneNumber, RecipientEntity recipient) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Try with original phone number first
        List<ConversationSentiment> sentiments = sentimentRepository.findUserSentimentsAfter(phoneNumber, weekAgo);

        // If no results, try with + prefix (WhatsApp stores with +)
        if (sentiments.isEmpty() && !phoneNumber.startsWith("+")) {
            sentiments = sentimentRepository.findUserSentimentsAfter("+" + phoneNumber, weekAgo);
        }

        // If still no results, try without + prefix
        if (sentiments.isEmpty() && phoneNumber.startsWith("+")) {
            sentiments = sentimentRepository.findUserSentimentsAfter(phoneNumber.substring(1), weekAgo);
        }

        Map<String, Object> report = new HashMap<>();

        // Basic info
        report.put("recipient_name", recipient.getDisplayName());
        report.put("phone_number", phoneNumber);
        report.put("report_period", "Last 7 Days");
        report.put("generated_at", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // Sentiment analysis
        Map<String, Object> sentimentAnalysis = analyzeSentiments(sentiments);
        report.put("sentiment_analysis", sentimentAnalysis);

        // Engagement metrics
        Map<String, Object> engagement = calculateEngagement(sentiments);
        report.put("engagement", engagement);

        // Notable moments (positive messages)
        List<String> notableMoments = extractNotableMoments(sentiments);
        report.put("notable_moments", notableMoments);

        // Topics from database
        List<RecipientTopicEntity> favouriteTopics = topicRepository.findByPhoneNumberAndTopicType(
            phoneNumber, RecipientTopicEntity.TopicType.FAVOURITE);
        List<RecipientTopicEntity> cheerUpTopics = topicRepository.findByPhoneNumberAndTopicType(
            phoneNumber, RecipientTopicEntity.TopicType.CHEER_UP);

        report.put("favourite_topics", favouriteTopics.stream()
            .map(RecipientTopicEntity::getTopic).collect(Collectors.toList()));
        report.put("cheer_up_topics", cheerUpTopics.stream()
            .map(RecipientTopicEntity::getTopic).collect(Collectors.toList()));

        // Action items
        List<String> actionItems = generateActionItems(sentimentAnalysis, cheerUpTopics);
        report.put("action_items", actionItems);

        // Alert status
        String alertStatus = determineAlertStatus(sentimentAnalysis);
        report.put("alert_status", alertStatus);
        report.put("alert_message", getAlertMessage(alertStatus));

        return report;
    }

    private Map<String, Object> analyzeSentiments(List<ConversationSentiment> sentiments) {
        Map<String, Object> analysis = new HashMap<>();

        if (sentiments.isEmpty()) {
            analysis.put("total_conversations", 0);
            analysis.put("green_count", 0);
            analysis.put("amber_count", 0);
            analysis.put("red_count", 0);
            analysis.put("green_percentage", 0);
            analysis.put("amber_percentage", 0);
            analysis.put("red_percentage", 0);
            analysis.put("trend", "No data");
            return analysis;
        }

        long greenCount = sentiments.stream()
            .filter(s -> s.getSentimentCategory() == SentimentAnalysis.SentimentCategory.GREEN)
            .count();
        long amberCount = sentiments.stream()
            .filter(s -> s.getSentimentCategory() == SentimentAnalysis.SentimentCategory.AMBER)
            .count();
        long redCount = sentiments.stream()
            .filter(s -> s.getSentimentCategory() == SentimentAnalysis.SentimentCategory.RED)
            .count();

        int total = sentiments.size();

        analysis.put("total_conversations", total);
        analysis.put("green_count", greenCount);
        analysis.put("amber_count", amberCount);
        analysis.put("red_count", redCount);
        analysis.put("green_percentage", Math.round((greenCount * 100.0) / total));
        analysis.put("amber_percentage", Math.round((amberCount * 100.0) / total));
        analysis.put("red_percentage", Math.round((redCount * 100.0) / total));

        // Calculate trend (compare first half to second half)
        String trend = calculateTrend(sentiments);
        analysis.put("trend", trend);

        // Daily breakdown for chart
        List<Map<String, Object>> dailyBreakdown = calculateDailyBreakdown(sentiments);
        analysis.put("daily_breakdown", dailyBreakdown);

        return analysis;
    }

    private String calculateTrend(List<ConversationSentiment> sentiments) {
        if (sentiments.size() < 2) return "Stable";

        int mid = sentiments.size() / 2;
        List<ConversationSentiment> recent = sentiments.subList(0, mid);
        List<ConversationSentiment> older = sentiments.subList(mid, sentiments.size());

        double recentScore = calculateAverageScore(recent);
        double olderScore = calculateAverageScore(older);

        double change = recentScore - olderScore;

        if (change > 10) return "Improving";
        if (change < -10) return "Declining";
        return "Stable";
    }

    private double calculateAverageScore(List<ConversationSentiment> sentiments) {
        if (sentiments.isEmpty()) return 50;

        return sentiments.stream()
            .mapToDouble(s -> {
                switch (s.getSentimentCategory()) {
                    case GREEN: return 100;
                    case AMBER: return 50;
                    case RED: return 0;
                    default: return 50;
                }
            })
            .average()
            .orElse(50);
    }

    private List<Map<String, Object>> calculateDailyBreakdown(List<ConversationSentiment> sentiments) {
        Map<String, List<ConversationSentiment>> byDate = sentiments.stream()
            .collect(Collectors.groupingBy(s ->
                s.getTimestamp().toLocalDate().toString()));

        return byDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                Map<String, Object> day = new HashMap<>();
                day.put("date", entry.getKey());
                day.put("score", Math.round(calculateAverageScore(entry.getValue())));
                return day;
            })
            .collect(Collectors.toList());
    }

    private Map<String, Object> calculateEngagement(List<ConversationSentiment> sentiments) {
        Map<String, Object> engagement = new HashMap<>();

        Set<String> activeDays = sentiments.stream()
            .map(s -> s.getTimestamp().toLocalDate().toString())
            .collect(Collectors.toSet());

        engagement.put("active_days", activeDays.size());
        engagement.put("total_messages", sentiments.size());
        engagement.put("engagement_level", activeDays.size() >= 5 ? "High" :
                                          activeDays.size() >= 3 ? "Medium" : "Low");

        return engagement;
    }

    private List<String> extractNotableMoments(List<ConversationSentiment> sentiments) {
        return sentiments.stream()
            .filter(s -> s.getSentimentCategory() == SentimentAnalysis.SentimentCategory.GREEN)
            .limit(3)
            .map(ConversationSentiment::getUserMessage)
            .filter(Objects::nonNull)
            .map(msg -> msg.length() > 100 ? msg.substring(0, 100) + "..." : msg)
            .collect(Collectors.toList());
    }

    private List<String> generateActionItems(Map<String, Object> sentimentAnalysis,
                                             List<RecipientTopicEntity> cheerUpTopics) {
        List<String> items = new ArrayList<>();

        String trend = (String) sentimentAnalysis.get("trend");
        if ("Declining".equals(trend)) {
            items.add("Consider reaching out with a phone call - mood appears to be declining");
        }

        long redCount = ((Number) sentimentAnalysis.get("red_count")).longValue();
        if (redCount > 0) {
            items.add("Review recent conversations for signs of distress");
        }

        if (!cheerUpTopics.isEmpty()) {
            String topics = cheerUpTopics.stream()
                .limit(2)
                .map(RecipientTopicEntity::getTopic)
                .collect(Collectors.joining(", "));
            items.add("Discuss topics that cheer them up: " + topics);
        }

        if (items.isEmpty()) {
            items.add("Continue regular check-ins - engagement is positive");
        }

        return items;
    }

    private String determineAlertStatus(Map<String, Object> sentimentAnalysis) {
        String trend = (String) sentimentAnalysis.get("trend");
        long redCount = ((Number) sentimentAnalysis.get("red_count")).longValue();
        int redPercentage = ((Number) sentimentAnalysis.get("red_percentage")).intValue();

        if (redPercentage > 30 || redCount >= 3) {
            return "RED";
        } else if ("Declining".equals(trend) || redPercentage > 10) {
            return "AMBER";
        }
        return "GREEN";
    }

    private String getAlertMessage(String status) {
        switch (status) {
            case "RED":
                return "Significant concern detected. Immediate attention recommended.";
            case "AMBER":
                return "A slight shift in mood detected. Consider reviewing recent insights.";
            default:
                return "Engagement is positive. Keep up the good work!";
        }
    }

    private boolean sendEmailReport(RecipientEntity recipient, Map<String, Object> reportData) {
        if (mailSender == null) {
            logger.warn("Mail sender not configured - cannot send email report");
            return false;
        }

        String email = recipient.getEmail();
        if (email == null || email.isEmpty()) {
            logger.warn("No email configured for recipient: {}", recipient.getPhoneNumber());
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Weekly Wellness Report - " + recipient.getDisplayName());
            helper.setText(buildEmailHtml(reportData), true);

            mailSender.send(message);
            logger.info("Email report sent to: {}", email);
            return true;
        } catch (MessagingException e) {
            logger.error("Failed to send email report: {}", e.getMessage());
            return false;
        }
    }

    private String buildEmailHtml(Map<String, Object> data) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append(".header { background: #4e9a91; color: white; padding: 20px; border-radius: 10px; }");
        html.append(".section { margin: 20px 0; padding: 15px; background: #f8f7f5; border-radius: 8px; }");
        html.append(".alert-green { border-left: 4px solid #a3b8a3; }");
        html.append(".alert-amber { border-left: 4px solid #f5b78a; }");
        html.append(".alert-red { border-left: 4px solid #e57373; }");
        html.append("</style></head><body>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>Weekly Wellness Report</h1>");
        html.append("<p>").append(data.get("recipient_name")).append(" - ").append(data.get("report_period")).append("</p>");
        html.append("</div>");

        // Alert
        String alertStatus = (String) data.get("alert_status");
        html.append("<div class='section alert-").append(alertStatus.toLowerCase()).append("'>");
        html.append("<strong>").append(data.get("alert_message")).append("</strong>");
        html.append("</div>");

        // Sentiment Analysis
        @SuppressWarnings("unchecked")
        Map<String, Object> sentiment = (Map<String, Object>) data.get("sentiment_analysis");
        html.append("<div class='section'>");
        html.append("<h2>Sentiment Analysis</h2>");
        html.append("<p>Trend: <strong>").append(sentiment.get("trend")).append("</strong></p>");
        html.append("<p>Positive: ").append(sentiment.get("green_percentage")).append("%</p>");
        html.append("<p>Concern: ").append(sentiment.get("amber_percentage")).append("%</p>");
        html.append("<p>Critical: ").append(sentiment.get("red_percentage")).append("%</p>");
        html.append("</div>");

        // Engagement
        @SuppressWarnings("unchecked")
        Map<String, Object> engagement = (Map<String, Object>) data.get("engagement");
        html.append("<div class='section'>");
        html.append("<h2>Engagement</h2>");
        html.append("<p>Active Days: ").append(engagement.get("active_days")).append(" of 7</p>");
        html.append("<p>Level: ").append(engagement.get("engagement_level")).append("</p>");
        html.append("</div>");

        // Action Items
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) data.get("action_items");
        html.append("<div class='section'>");
        html.append("<h2>Recommended Actions</h2>");
        html.append("<ul>");
        for (String action : actions) {
            html.append("<li>").append(action).append("</li>");
        }
        html.append("</ul>");
        html.append("</div>");

        html.append("<p style='color: #666; font-size: 12px;'>Generated by AI Companion - ").append(data.get("generated_at")).append("</p>");
        html.append("</body></html>");

        return html.toString();
    }

    private boolean sendWhatsAppReport(RecipientEntity recipient, Map<String, Object> reportData) {
        String targetPhone = recipient.getCaretakerPhoneNumber();
        if (targetPhone == null || targetPhone.isEmpty()) {
            targetPhone = recipient.getPhoneNumber();
        }

        String message = buildWhatsAppMessage(reportData);

        try {
            whatsAppService.sendWhatsAppMessage(targetPhone, message);
            logger.info("WhatsApp report sent to: {}", targetPhone);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send WhatsApp report: {}", e.getMessage());
            return false;
        }
    }

    private String buildWhatsAppMessage(Map<String, Object> data) {
        StringBuilder msg = new StringBuilder();

        msg.append("📊 *Weekly Wellness Report*\n");
        msg.append("👤 ").append(data.get("recipient_name")).append("\n");
        msg.append("📅 ").append(data.get("report_period")).append("\n\n");

        // Alert
        String alertStatus = (String) data.get("alert_status");
        String alertEmoji = "GREEN".equals(alertStatus) ? "✅" :
                          "AMBER".equals(alertStatus) ? "⚠️" : "🚨";
        msg.append(alertEmoji).append(" ").append(data.get("alert_message")).append("\n\n");

        // Sentiment
        @SuppressWarnings("unchecked")
        Map<String, Object> sentiment = (Map<String, Object>) data.get("sentiment_analysis");
        msg.append("*Sentiment Analysis*\n");
        msg.append("📈 Trend: ").append(sentiment.get("trend")).append("\n");
        msg.append("🟢 Positive: ").append(sentiment.get("green_percentage")).append("%\n");
        msg.append("🟡 Concern: ").append(sentiment.get("amber_percentage")).append("%\n");
        msg.append("🔴 Critical: ").append(sentiment.get("red_percentage")).append("%\n\n");

        // Engagement
        @SuppressWarnings("unchecked")
        Map<String, Object> engagement = (Map<String, Object>) data.get("engagement");
        msg.append("*Engagement*\n");
        msg.append("📱 Active Days: ").append(engagement.get("active_days")).append("/7\n");
        msg.append("💪 Level: ").append(engagement.get("engagement_level")).append("\n\n");

        // Actions
        @SuppressWarnings("unchecked")
        List<String> actions = (List<String>) data.get("action_items");
        msg.append("*Recommended Actions*\n");
        for (String action : actions) {
            msg.append("• ").append(action).append("\n");
        }

        msg.append("\n🤖 _Generated by AI Companion_");

        return msg.toString();
    }
}
