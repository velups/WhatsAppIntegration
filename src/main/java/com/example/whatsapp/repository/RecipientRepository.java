package com.example.whatsapp.repository;

import com.example.whatsapp.entity.RecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientRepository extends JpaRepository<RecipientEntity, Long> {

    Optional<RecipientEntity> findByPhoneNumber(String phoneNumber);

    List<RecipientEntity> findByEnabledTrue();

    @Query(value = "SELECT * FROM recipients r WHERE r.enabled = true AND " +
           "(r.last_check_sent IS NULL OR r.last_check_sent < NOW() - INTERVAL '20 hours')",
           nativeQuery = true)
    List<RecipientEntity> findRecipientsDueForCheck();

    boolean existsByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);
}
