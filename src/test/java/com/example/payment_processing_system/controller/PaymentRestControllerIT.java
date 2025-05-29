package com.example.payment_processing_system.controller;

import com.example.payment_processing_system.PaymentProcessingSystemApplication;
import com.example.payment_processing_system.config.TestSecurityConfig;
import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.entity.BankCardInfoEntity;
import com.example.payment_processing_system.entity.PaymentEntity;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.BankCardInfoRepository;
import com.example.payment_processing_system.repository.PaymentRepository;
import com.example.payment_processing_system.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentRestControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BankCardInfoRepository bankCardInfoRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String userToken;
    private UUID userId;
    private UUID paymentId;

    @BeforeEach
    @Transactional
    void setup() {
        paymentRepository.deleteAll();
        bankCardInfoRepository.deleteAll();
        accountRepository.deleteAll();

        AccountEntity user = accountRepository.save(AccountEntity.builder()
                .email("st7936246@stud.duikt.edu.ua")
                .passwordHash("password")
                .username("testuser")
                .role(Role.ADMIN)
                .build());

        userId = user.getId();
        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        bankCardInfoRepository.save(BankCardInfoEntity.builder()
                .cardNumber("5375414120598205")
                .account(user)
                .balance(BigDecimal.valueOf(10000))
                .cardExpiryDate("12/25")
                .cvv(123)
                .build());

        PaymentEntity paymentEntity = paymentRepository.save(PaymentEntity.builder()
                .amount(BigDecimal.valueOf(100))
                .timestamp(LocalDateTime.now())
                .status("COMPLETED")
                .account(user)
                .build());

        paymentId = paymentEntity.getId();
    }

    @Test
    void shouldProcessPayment() {
        PaymentRequestDTO paymentRequest = PaymentRequestDTO.builder()
                .email("st7936246@stud.duikt.edu.ua")
                .cardNumber("5375414120598205")
                .cardExpiryDate("12/25")
                .cvv(123)
                .amount(100)
                .build();

        webTestClient.post()
                .uri("/api/v1/payments/process")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                });
    }

    @Test
    void shouldDownloadInvoice() {
        webTestClient.get()
                .uri("/api/v1/payments/invoice/{transactionId}", paymentId)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .consumeWith(response -> {
                    assertNotNull(response.getResponseBody());
                });
    }
}
