package com.example.payment_processing_system.repository;

import com.example.payment_processing_system.entity.BankCardInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankCardInfoRepository extends JpaRepository<BankCardInfoEntity, String> {
    Optional<BankCardInfoEntity> findByCardNumber(String cardNumber);
}
