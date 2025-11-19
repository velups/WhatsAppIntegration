package com.example.whatsapp.repository;

import com.example.whatsapp.entity.ConversationSentiment;
import com.example.whatsapp.dto.SentimentAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConversationSentimentRepository extends JpaRepository<ConversationSentiment, Long> {
    
    List<ConversationSentiment> findByPhoneNumberOrderByTimestampDesc(String phoneNumber);
    
    List<ConversationSentiment> findByPhoneNumberAndTimestampAfterOrderByTimestampDesc(
            String phoneNumber, LocalDateTime after);
    
    List<ConversationSentiment> findBySentimentCategoryOrderByTimestampDesc(
            SentimentAnalysis.SentimentCategory category);
    
    List<ConversationSentiment> findByRequiresAttentionTrueOrderByTimestampDesc();
    
    @Query("SELECT cs FROM ConversationSentiment cs WHERE cs.timestamp >= :startDate ORDER BY cs.timestamp DESC")
    List<ConversationSentiment> findRecentSentiments(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT cs.sentimentCategory, COUNT(cs) FROM ConversationSentiment cs " +
           "WHERE cs.timestamp >= :startDate GROUP BY cs.sentimentCategory")
    List<Object[]> getSentimentCounts(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT cs FROM ConversationSentiment cs WHERE cs.phoneNumber = :phoneNumber " +
           "AND cs.timestamp >= :startDate ORDER BY cs.timestamp DESC")
    List<ConversationSentiment> findUserSentimentsAfter(
            @Param("phoneNumber") String phoneNumber, 
            @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT DISTINCT cs.phoneNumber FROM ConversationSentiment cs " +
           "WHERE cs.sentimentCategory = 'RED' AND cs.timestamp >= :startDate")
    List<String> findUsersWithRecentRedSentiments(@Param("startDate") LocalDateTime startDate);
}