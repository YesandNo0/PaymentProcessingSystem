package com.example.payment_processing_system.domain;

import com.example.payment_processing_system.domain.enums.Status;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class PaymentDTO {
    UUID id;
    BigDecimal amount;
    LocalDateTime timestamp;
    Status status;
    String cardNumber;
    UUID accountId;
} 