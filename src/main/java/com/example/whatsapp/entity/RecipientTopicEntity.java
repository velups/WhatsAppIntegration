package com.example.whatsapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recipient_topics")
public class RecipientTopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "topic_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TopicType topicType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum TopicType {
        FAVOURITE,      // Topics the recipient enjoys discussing
        CHEER_UP,       // Topics to use when recipient needs cheering up
        AVOID           // Topics to avoid in conversation
    }

    public RecipientTopicEntity() {
        this.createdAt = LocalDateTime.now();
    }

    public RecipientTopicEntity(String phoneNumber, String topic, TopicType topicType) {
        this.phoneNumber = phoneNumber;
        this.topic = topic;
        this.topicType = topicType;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public TopicType getTopicType() {
        return topicType;
    }

    public void setTopicType(TopicType topicType) {
        this.topicType = topicType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
