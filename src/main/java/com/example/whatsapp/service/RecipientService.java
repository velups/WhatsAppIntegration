package com.example.whatsapp.service;

import com.example.whatsapp.entity.RecipientEntity;
import com.example.whatsapp.repository.RecipientRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecipientService {

    private static final Logger log = LoggerFactory.getLogger(RecipientService.class);

    private final RecipientRepository recipientRepository;

    public RecipientService(RecipientRepository recipientRepository) {
        this.recipientRepository = recipientRepository;
    }

    @PostConstruct
    public void init() {
        // Initialize with sample data if database is empty
        if (recipientRepository.count() == 0) {
            log.info("No recipients found in database. Creating sample recipients.");
            createSampleRecipients();
        } else {
            log.info("Loaded {} recipients from database", recipientRepository.count());
        }
    }

    private void createSampleRecipients() {
        RecipientEntity sample1 = new RecipientEntity();
        sample1.setPhoneNumber("+1234567890");
        sample1.setName("Mary");
        sample1.setPreferredTimeOfDay("morning");
        sample1.setEnabled(false);
        sample1.setTimezone("America/New_York");
        sample1.setRelationship("friend");
        sample1.setNotes("Sample recipient - update with real data");
        recipientRepository.save(sample1);

        RecipientEntity sample2 = new RecipientEntity();
        sample2.setPhoneNumber("+0987654321");
        sample2.setName("John");
        sample2.setPreferredTimeOfDay("evening");
        sample2.setCustomMessage("Hope you had a wonderful day!");
        sample2.setEnabled(false);
        sample2.setTimezone("Asia/Singapore");
        sample2.setRelationship("family");
        sample2.setNotes("Sample recipient - update with real data");
        recipientRepository.save(sample2);

        log.info("Created 2 sample recipients");
    }

    public List<RecipientEntity> getAllRecipients() {
        return recipientRepository.findAll();
    }

    public List<RecipientEntity> getEnabledRecipients() {
        return recipientRepository.findByEnabledTrue();
    }

    public List<RecipientEntity> getRecipientsDueForCheck() {
        return recipientRepository.findRecipientsDueForCheck();
    }

    public Optional<RecipientEntity> getRecipientByPhoneNumber(String phoneNumber) {
        // Normalize phone number for comparison (remove + prefix if present)
        String normalizedInput = phoneNumber != null ? phoneNumber.replaceAll("^\\+", "") : "";

        // Try exact match first
        Optional<RecipientEntity> result = recipientRepository.findByPhoneNumber(phoneNumber);
        if (result.isPresent()) {
            return result;
        }

        // Try with + prefix
        result = recipientRepository.findByPhoneNumber("+" + normalizedInput);
        if (result.isPresent()) {
            return result;
        }

        // Try without + prefix
        return recipientRepository.findByPhoneNumber(normalizedInput);
    }

    public Optional<RecipientEntity> getRecipientById(Long id) {
        return recipientRepository.findById(id);
    }

    @Transactional
    public void updateLastCheckSent(String phoneNumber) {
        getRecipientByPhoneNumber(phoneNumber).ifPresent(r -> {
            r.setLastCheckSent(LocalDateTime.now());
            recipientRepository.save(r);
            log.debug("Updated last check sent time for {}", r.getName());
        });
    }

    @Transactional
    public RecipientEntity addRecipient(RecipientEntity recipient) {
        if (recipientRepository.existsByPhoneNumber(recipient.getPhoneNumber())) {
            throw new IllegalArgumentException("Recipient with phone number " + recipient.getPhoneNumber() + " already exists");
        }
        RecipientEntity saved = recipientRepository.save(recipient);
        log.info("Added new recipient: {} ({})", saved.getName(), saved.getPhoneNumber());
        return saved;
    }

    @Transactional
    public RecipientEntity updateRecipient(String phoneNumber, RecipientEntity updatedData) {
        RecipientEntity existing = getRecipientByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found: " + phoneNumber));

        // Update fields if provided
        if (updatedData.getName() != null) {
            existing.setName(updatedData.getName());
        }
        if (updatedData.getPreferredTimeOfDay() != null) {
            existing.setPreferredTimeOfDay(updatedData.getPreferredTimeOfDay());
        }
        if (updatedData.getCustomMessage() != null) {
            existing.setCustomMessage(updatedData.getCustomMessage());
        }
        existing.setEnabled(updatedData.isEnabled());
        if (updatedData.getTimezone() != null) {
            existing.setTimezone(updatedData.getTimezone());
        }
        if (updatedData.getRelationship() != null) {
            existing.setRelationship(updatedData.getRelationship());
        }
        if (updatedData.getNotes() != null) {
            existing.setNotes(updatedData.getNotes());
        }
        if (updatedData.getCaretakerPhoneNumber() != null) {
            existing.setCaretakerPhoneNumber(updatedData.getCaretakerPhoneNumber());
        }
        if (updatedData.getCaretakerName() != null) {
            existing.setCaretakerName(updatedData.getCaretakerName());
        }

        RecipientEntity saved = recipientRepository.save(existing);
        log.info("Updated recipient: {} ({})", saved.getName(), saved.getPhoneNumber());
        return saved;
    }

    @Transactional
    public boolean removeRecipient(String phoneNumber) {
        Optional<RecipientEntity> recipient = getRecipientByPhoneNumber(phoneNumber);
        if (recipient.isPresent()) {
            recipientRepository.delete(recipient.get());
            log.info("Removed recipient with phone number: {}", phoneNumber);
            return true;
        }
        return false;
    }

    @Transactional
    public void enableRecipient(String phoneNumber, boolean enabled) {
        getRecipientByPhoneNumber(phoneNumber).ifPresent(r -> {
            r.setEnabled(enabled);
            recipientRepository.save(r);
            log.info("{} wellness checks for {} ({})",
                    enabled ? "Enabled" : "Disabled", r.getName(), phoneNumber);
        });
    }

    public int getEnabledRecipientsCount() {
        return (int) recipientRepository.findByEnabledTrue().size();
    }

    public long getTotalRecipientsCount() {
        return recipientRepository.count();
    }
}
