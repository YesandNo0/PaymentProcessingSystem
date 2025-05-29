package com.example.payment_processing_system.repository;

import com.example.payment_processing_system.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, String> {
    Optional<AccountEntity> findByEmail(String email);
    Optional<AccountEntity> findByUsername(String username);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
