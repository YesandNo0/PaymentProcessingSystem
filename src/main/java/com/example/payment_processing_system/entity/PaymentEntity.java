package com.example.payment_processing_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "payments")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false)
    private UUID id;

    private BigDecimal amount;
    private LocalDateTime timestamp;
    private String status;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private AccountEntity account;
} 