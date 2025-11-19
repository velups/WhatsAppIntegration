package com.example.whatsapp.controller;

import com.example.whatsapp.entity.RecipientTopicEntity;
import com.example.whatsapp.entity.RecipientTopicEntity.TopicType;
import com.example.whatsapp.repository.RecipientTopicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recipients/{phoneNumber}/topics")
public class RecipientTopicController {

    private final RecipientTopicRepository topicRepository;

    public RecipientTopicController(RecipientTopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    /**
     * Get all topics for a recipient
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTopics(@PathVariable String phoneNumber) {
        List<RecipientTopicEntity> allTopics = topicRepository.findByPhoneNumber(phoneNumber);

        Map<String, List<String>> topicsByType = new HashMap<>();
        topicsByType.put("favourite", filterTopicsByType(allTopics, TopicType.FAVOURITE));
        topicsByType.put("cheer_up", filterTopicsByType(allTopics, TopicType.CHEER_UP));
        topicsByType.put("avoid", filterTopicsByType(allTopics, TopicType.AVOID));

        Map<String, Object> response = new HashMap<>();
        response.put("phone_number", phoneNumber);
        response.put("topics", topicsByType);
        response.put("total_count", allTopics.size());

        return ResponseEntity.ok(response);
    }

    /**
     * Get favourite topics for a recipient
     */
    @GetMapping("/favourite")
    public ResponseEntity<Map<String, Object>> getFavouriteTopics(@PathVariable String phoneNumber) {
        List<RecipientTopicEntity> topics = topicRepository.findByPhoneNumberAndTopicType(
                phoneNumber, TopicType.FAVOURITE);
        return buildTopicResponse(phoneNumber, "favourite", topics);
    }

    /**
     * Get cheer-up topics for a recipient
     */
    @GetMapping("/cheer-up")
    public ResponseEntity<Map<String, Object>> getCheerUpTopics(@PathVariable String phoneNumber) {
        List<RecipientTopicEntity> topics = topicRepository.findByPhoneNumberAndTopicType(
                phoneNumber, TopicType.CHEER_UP);
        return buildTopicResponse(phoneNumber, "cheer_up", topics);
    }

    /**
     * Get topics to avoid for a recipient
     */
    @GetMapping("/avoid")
    public ResponseEntity<Map<String, Object>> getAvoidTopics(@PathVariable String phoneNumber) {
        List<RecipientTopicEntity> topics = topicRepository.findByPhoneNumberAndTopicType(
                phoneNumber, TopicType.AVOID);
        return buildTopicResponse(phoneNumber, "avoid", topics);
    }

    /**
     * Add favourite topics (accepts array of topics)
     */
    @PostMapping("/favourite")
    public ResponseEntity<Map<String, Object>> addFavouriteTopics(
            @PathVariable String phoneNumber,
            @RequestBody List<String> topics) {
        return addTopics(phoneNumber, topics, TopicType.FAVOURITE, "favourite");
    }

    /**
     * Add cheer-up topics (accepts array of topics)
     */
    @PostMapping("/cheer-up")
    public ResponseEntity<Map<String, Object>> addCheerUpTopics(
            @PathVariable String phoneNumber,
            @RequestBody List<String> topics) {
        return addTopics(phoneNumber, topics, TopicType.CHEER_UP, "cheer_up");
    }

    /**
     * Add topics to avoid (accepts array of topics)
     */
    @PostMapping("/avoid")
    public ResponseEntity<Map<String, Object>> addAvoidTopics(
            @PathVariable String phoneNumber,
            @RequestBody List<String> topics) {
        return addTopics(phoneNumber, topics, TopicType.AVOID, "avoid");
    }

    /**
     * Replace all favourite topics
     */
    @PutMapping("/favourite")
    @Transactional
    public ResponseEntity<Map<String, Object>> replaceFavouriteTopics(
            @PathVariable String phoneNumber,
            @RequestBody List<String> topics) {
        return replaceTopics(phoneNumber, topics, TopicType.FAVOURITE, "favourite");
    }

    /**
     * Replace all cheer-up topics
     */
    @PutMapping("/cheer-up")
    @Transactional
    public ResponseEntity<Map<String, Object>> replaceCheerUpTopics(
            @PathVariable String phoneNumber,
            @RequestBody List<String> topics) {
        return replaceTopics(phoneNumber, topics, TopicType.CHEER_UP, "cheer_up");
    }

    /**
     * Replace all avoid topics
     */
    @PutMapping("/avoid")
    @Transactional
    public ResponseEntity<Map<String, Object>> replaceAvoidTopics(
            @PathVariable String phoneNumber,
            @RequestBody List<String> topics) {
        return replaceTopics(phoneNumber, topics, TopicType.AVOID, "avoid");
    }

    /**
     * Delete a specific topic
     */
    @DeleteMapping("/{topicType}/{topic}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteTopic(
            @PathVariable String phoneNumber,
            @PathVariable String topicType,
            @PathVariable String topic) {

        TopicType type = parseTopicType(topicType);
        if (type == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid topic type. Use: favourite, cheer-up, or avoid"
            ));
        }

        topicRepository.deleteByPhoneNumberAndTopicAndTopicType(phoneNumber, topic, type);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Topic deleted successfully",
                "phone_number", phoneNumber,
                "topic", topic,
                "topic_type", topicType
        ));
    }

    /**
     * Delete all topics of a specific type
     */
    @DeleteMapping("/{topicType}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteAllTopicsOfType(
            @PathVariable String phoneNumber,
            @PathVariable String topicType) {

        TopicType type = parseTopicType(topicType);
        if (type == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid topic type. Use: favourite, cheer-up, or avoid"
            ));
        }

        topicRepository.deleteByPhoneNumberAndTopicType(phoneNumber, type);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All " + topicType + " topics deleted",
                "phone_number", phoneNumber
        ));
    }

    // Helper methods

    private ResponseEntity<Map<String, Object>> addTopics(
            String phoneNumber, List<String> topics, TopicType type, String typeName) {

        int added = 0;
        int skipped = 0;

        for (String topic : topics) {
            if (topic == null || topic.trim().isEmpty()) {
                skipped++;
                continue;
            }

            String trimmedTopic = topic.trim();
            if (!topicRepository.existsByPhoneNumberAndTopicAndTopicType(phoneNumber, trimmedTopic, type)) {
                topicRepository.save(new RecipientTopicEntity(phoneNumber, trimmedTopic, type));
                added++;
            } else {
                skipped++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("phone_number", phoneNumber);
        response.put("topic_type", typeName);
        response.put("added", added);
        response.put("skipped", skipped);
        response.put("message", String.format("Added %d topics, skipped %d (duplicates or empty)", added, skipped));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private ResponseEntity<Map<String, Object>> replaceTopics(
            String phoneNumber, List<String> topics, TopicType type, String typeName) {

        // Delete existing topics of this type
        topicRepository.deleteByPhoneNumberAndTopicType(phoneNumber, type);

        // Add new topics
        int added = 0;
        for (String topic : topics) {
            if (topic != null && !topic.trim().isEmpty()) {
                topicRepository.save(new RecipientTopicEntity(phoneNumber, topic.trim(), type));
                added++;
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("phone_number", phoneNumber);
        response.put("topic_type", typeName);
        response.put("total_topics", added);
        response.put("message", String.format("Replaced with %d topics", added));

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<Map<String, Object>> buildTopicResponse(
            String phoneNumber, String typeName, List<RecipientTopicEntity> topics) {

        List<String> topicList = topics.stream()
                .map(RecipientTopicEntity::getTopic)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("phone_number", phoneNumber);
        response.put("topic_type", typeName);
        response.put("topics", topicList);
        response.put("count", topicList.size());

        return ResponseEntity.ok(response);
    }

    private List<String> filterTopicsByType(List<RecipientTopicEntity> allTopics, TopicType type) {
        return allTopics.stream()
                .filter(t -> t.getTopicType() == type)
                .map(RecipientTopicEntity::getTopic)
                .collect(Collectors.toList());
    }

    private TopicType parseTopicType(String typeName) {
        switch (typeName.toLowerCase()) {
            case "favourite":
            case "favorite":
                return TopicType.FAVOURITE;
            case "cheer-up":
            case "cheerup":
            case "cheer_up":
                return TopicType.CHEER_UP;
            case "avoid":
                return TopicType.AVOID;
            default:
                return null;
        }
    }
}
