package com.example.payment_processing_system.domain;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PaymentRequestDTO {
    String email;
    String cardNumber;
    String cardExpiryDate;
    int cvv;
    double amount;
} 