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

    @Query("SELECT r FROM RecipientEntity r WHERE r.enabled = true AND " +
           "(r.lastCheckSent IS NULL OR r.lastCheckSent < CURRENT_TIMESTAMP - 20 * 60 * 60)")
    List<RecipientEntity> findRecipientsDueForCheck();

    boolean existsByPhoneNumber(String phoneNumber);

    void deleteByPhoneNumber(String phoneNumber);
}
