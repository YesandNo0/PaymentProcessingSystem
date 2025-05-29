package com.example.payment_processing_system.service.impl;

import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.entity.PaymentEntity;
import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.domain.enums.Status;
import com.example.payment_processing_system.exception.AccountNotFoundException;
import com.example.payment_processing_system.exception.AuthenticationException;
import com.example.payment_processing_system.gateway.PaymentGateway;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.PaymentRepository;
import com.example.payment_processing_system.service.EmailService;
import com.example.payment_processing_system.service.PaymentService;
import com.example.payment_processing_system.validation.PaymentValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentValidator paymentValidator;
    private final PaymentGateway paymentGateway;
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest) {
        String userEmail = accountRepository.findByEmail(paymentRequest.getEmail())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"))
                .getEmail();

        paymentValidator.validatePayment(paymentRequest);
        PaymentResponseDTO paymentResponse = paymentGateway.processPayment(paymentRequest);
        if (Objects.equals(paymentResponse.getStatus(), String.valueOf(Status.APPROVED))) {
            UUID transactionId = savePaymentHistory(userEmail, paymentRequest, paymentResponse).getId();
            paymentResponse = paymentResponse.toBuilder()
                    .transactionId(transactionId)
                    .status(Status.APPROVED.name())
                    .amount(paymentRequest.getAmount())
                    .build();
            sendPaymentConfirmationEmail(userEmail, paymentResponse, paymentRequest);
        }
        return paymentResponse;
    }

    @Transactional
    public PaymentEntity savePaymentHistory(String userEmail, PaymentRequestDTO paymentRequest, PaymentResponseDTO paymentResponse) {
        AccountEntity account = accountRepository.findByEmail(userEmail).orElseThrow(() -> new AccountNotFoundException("Account not found: " + userEmail));
        return paymentRepository.save(PaymentEntity.builder()
                .account(account)
                .amount(BigDecimal.valueOf(paymentRequest.getAmount()))
                .status(String.valueOf(paymentResponse.getStatus()))
                .timestamp(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findById(UUID.fromString(transactionId))
                .map(payment -> PaymentResponseDTO.builder()
                        .transactionId(payment.getId())
                        .status(payment.getStatus())
                        .amount(payment.getAmount().doubleValue())
                        .build())
                .orElseThrow(() -> new RuntimeException("Transaction not found"));
    }


    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return null;
        }
    }

    private void sendPaymentConfirmationEmail(String userEmail, PaymentResponseDTO paymentResponse, PaymentRequestDTO paymentRequest) {
        String subject = "Payment Confirmation";
        String message = String.format(
                "Your payment was successfully processed.\nTransaction ID: %s\nStatus: %s\nAmount: %.2f",
                paymentResponse.getTransactionId(), paymentResponse.getStatus(), paymentRequest.getAmount());

        emailService.sendEmail(userEmail, subject, message);
    }
}