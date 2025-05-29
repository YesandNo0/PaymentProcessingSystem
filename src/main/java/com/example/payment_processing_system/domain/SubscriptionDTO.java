package com.example.payment_processing_system.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class SubscriptionDTO {
    UUID subscriptionId;
    UUID accountId;
    LocalDate startDate;
    LocalDate endDate;
    BigDecimal amount;
    String status;
} 