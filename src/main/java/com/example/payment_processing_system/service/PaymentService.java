package com.example.payment_processing_system.service;

import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;


public interface PaymentService {
    PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest);

    PaymentResponseDTO getPaymentByTransactionId(String transactionId);
}