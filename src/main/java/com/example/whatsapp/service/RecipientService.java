package com.example.whatsapp.service;

import com.example.whatsapp.model.Recipient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipientService {
    
    private static final Logger log = LoggerFactory.getLogger(RecipientService.class);
    
    @Value("${wellness.recipients.config-file:recipients.json}")
    private String configFileName;
    
    private List<Recipient> recipients = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @PostConstruct
    public void loadRecipients() {
        try {
            ClassPathResource resource = new ClassPathResource(configFileName);
            if (resource.exists()) {
                InputStream inputStream = resource.getInputStream();
                recipients = objectMapper.readValue(inputStream, new TypeReference<List<Recipient>>() {});
                log.info("Loaded {} recipients from {}", recipients.size(), configFileName);
            } else {
                log.warn("Recipients configuration file {} not found. Creating sample recipients.", configFileName);
                createSampleRecipients();
            }
        } catch (IOException e) {
            log.error("Error loading recipients configuration: {}", e.getMessage());
            createSampleRecipients();
        }
    }
    
    private void createSampleRecipients() {
        recipients = List.of(
            Recipient.builder()
                .phoneNumber("+1234567890")
                .name("Mary")
                .preferredTimeOfDay("morning")
                .enabled(false) // Disabled by default for safety
                .timezone("America/New_York")
                .relationship("friend")
                .notes("Sample recipient - update with real data")
                .build(),
            Recipient.builder()
                .phoneNumber("+0987654321")
                .name("John")
                .preferredTimeOfDay("evening")
                .customMessage("Hope you had a wonderful day!")
                .enabled(false) // Disabled by default for safety
                .timezone("Asia/Singapore")
                .relationship("family")
                .notes("Sample recipient - update with real data")
                .build()
        );
        log.info("Created {} sample recipients", recipients.size());
    }
    
    public List<Recipient> getAllRecipients() {
        return new ArrayList<>(recipients);
    }
    
    public List<Recipient> getEnabledRecipients() {
        return recipients.stream()
                .filter(Recipient::isEnabled)
                .collect(Collectors.toList());
    }
    
    public List<Recipient> getRecipientsDueForCheck() {
        return recipients.stream()
                .filter(Recipient::shouldSendCheck)
                .collect(Collectors.toList());
    }
    
    public Optional<Recipient> getRecipientByPhoneNumber(String phoneNumber) {
        // Normalize phone number for comparison (remove + prefix if present)
        String normalizedInput = phoneNumber != null ? phoneNumber.replaceAll("^\\+", "") : "";

        return recipients.stream()
                .filter(r -> {
                    String normalizedRecipient = r.getPhoneNumber() != null
                        ? r.getPhoneNumber().replaceAll("^\\+", "")
                        : "";
                    return normalizedRecipient.equals(normalizedInput);
                })
                .findFirst();
    }
    
    public void updateLastCheckSent(String phoneNumber) {
        recipients.stream()
                .filter(r -> r.getPhoneNumber().equals(phoneNumber))
                .findFirst()
                .ifPresent(r -> {
                    r.setLastCheckSent(LocalDateTime.now());
                    log.debug("Updated last check sent time for {}", r.getName());
                });
    }
    
    public void addRecipient(Recipient recipient) {
        recipients.add(recipient);
        log.info("Added new recipient: {} ({})", recipient.getName(), recipient.getPhoneNumber());
    }
    
    public boolean removeRecipient(String phoneNumber) {
        boolean removed = recipients.removeIf(r -> r.getPhoneNumber().equals(phoneNumber));
        if (removed) {
            log.info("Removed recipient with phone number: {}", phoneNumber);
        }
        return removed;
    }
    
    public void enableRecipient(String phoneNumber, boolean enabled) {
        getRecipientByPhoneNumber(phoneNumber).ifPresent(r -> {
            r.setEnabled(enabled);
            log.info("{} wellness checks for {} ({})", 
                    enabled ? "Enabled" : "Disabled", r.getName(), phoneNumber);
        });
    }
    
    public int getEnabledRecipientsCount() {
        return (int) recipients.stream().filter(Recipient::isEnabled).count();
    }
}