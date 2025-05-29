package com.example.payment_processing_system.controller;

import com.example.payment_processing_system.PaymentProcessingSystemApplication;
import com.example.payment_processing_system.config.TestSecurityConfig;
import com.example.payment_processing_system.domain.SubscriptionDTO;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.entity.SubscriptionEntity;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.SubscriptionRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SubscriptionRestControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private UUID adminId;
    private UUID subscriptionId;

    @BeforeEach
    void setup() {
        subscriptionRepository.deleteAll();
        accountRepository.deleteAll();

        AccountEntity admin = accountRepository.save(AccountEntity.builder()
                .email("st7936246@stud.duikt.edu.ua")
                .passwordHash("password")
                .username("admin")
                .role(Role.ADMIN)
                .build());
        adminId = admin.getId();

        SubscriptionEntity subscriptionEntity = subscriptionRepository.save(SubscriptionEntity.builder()
                .account(admin)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusMonths(1))
                .amount(BigDecimal.valueOf(100))
                .status("ACTIVE")
                .build());

        adminToken = jwtTokenProvider.createToken(admin.getEmail(), admin.getRole());
        subscriptionId = subscriptionEntity.getSubscriptionId();
    }

    @Test
    void shouldCreateSubscription() {
        SubscriptionDTO newSubscription = SubscriptionDTO.builder()
                .accountId(adminId)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusMonths(1))
                .amount(BigDecimal.valueOf(100))
                .status("ACTIVE")
                .build();

        webTestClient.post()
                .uri("/api/v1/subscriptions/create?userEmail=st7936246@stud.duikt.edu.ua")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newSubscription)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionDTO.class)
                .consumeWith(response -> {
                    SubscriptionDTO createdSubscription = response.getResponseBody();
                    assertNotNull(createdSubscription);
                    assertEquals(100, createdSubscription.getAmount().doubleValue());
                });
    }

    @Test
    void shouldGetUserSubscriptions() {
        webTestClient.get()
                .uri("/api/v1/subscriptions/user?userEmail=st7936246@stud.duikt.edu.ua")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SubscriptionDTO.class)
                .consumeWith(response -> {
                    assertFalse(response.getResponseBody().isEmpty());
                });
    }

    @Test
    void shouldCancelSubscription() {
        webTestClient.put()
                .uri("/api/v1/subscriptions/cancel/{subscriptionId}", subscriptionId)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionDTO.class)
                .consumeWith(response -> {
                    SubscriptionDTO canceledSubscription = response.getResponseBody();
                    assertNotNull(canceledSubscription);
                    assertEquals("CANCELLED", canceledSubscription.getStatus());
                });
    }
}
