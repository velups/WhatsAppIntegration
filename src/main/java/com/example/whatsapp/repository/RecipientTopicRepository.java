package com.example.whatsapp.repository;

import com.example.whatsapp.entity.RecipientTopicEntity;
import com.example.whatsapp.entity.RecipientTopicEntity.TopicType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipientTopicRepository extends JpaRepository<RecipientTopicEntity, Long> {

    List<RecipientTopicEntity> findByPhoneNumber(String phoneNumber);

    List<RecipientTopicEntity> findByPhoneNumberAndTopicType(String phoneNumber, TopicType topicType);

    void deleteByPhoneNumberAndTopicAndTopicType(String phoneNumber, String topic, TopicType topicType);

    void deleteByPhoneNumberAndTopicType(String phoneNumber, TopicType topicType);

    void deleteByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumberAndTopicAndTopicType(String phoneNumber, String topic, TopicType topicType);
}
