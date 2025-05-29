package com.example.payment_processing_system.domain;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class PaymentResponseDTO {
    UUID transactionId;
    String status;
    double amount;
    String error;
} 