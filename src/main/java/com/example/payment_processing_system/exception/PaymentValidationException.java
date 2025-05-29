package com.example.payment_processing_system.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String message) {
        super(message);
    }
} 