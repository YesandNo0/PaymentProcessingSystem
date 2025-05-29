package com.example.payment_processing_system.repository;

import com.example.payment_processing_system.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {
    List<SubscriptionEntity> findByAccountEmail(String email);
    Optional<SubscriptionEntity> findByAccountEmailAndStatus(String email, String status);
}
