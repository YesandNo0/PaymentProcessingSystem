package com.example.payment_processing_system.controller;

import com.example.payment_processing_system.domain.AccountDTO;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;


import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountRestControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private String userToken;
    private String testEmail;

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();

        AccountEntity admin = accountRepository.save(AccountEntity.builder()
                .email("admin@example.com")
                .passwordHash("password")
                .username("admin")
                .role(Role.ADMIN)
                .build());

        AccountEntity user = accountRepository.save(AccountEntity.builder()
                .email("user@example.com")
                .passwordHash("password")
                .username("user")
                .role(Role.USER)
                .build());

        adminToken = jwtTokenProvider.createToken(admin.getEmail(), admin.getRole());
        userToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());
        testEmail = user.getEmail();
    }

    @Test
    void shouldCreateAccount() {
        AccountDTO newAccount = AccountDTO.builder()
                .email("new@example.com")
                .password("password")
                .username("newuser")
                .build();

        webTestClient.post()
                .uri("/api/v1/accounts/create")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newAccount)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountDTO.class)
                .consumeWith(response -> {
                    AccountDTO createdAccount = response.getResponseBody();
                    assertNotNull(createdAccount);
                    assertEquals("new@example.com", createdAccount.getEmail());
                    assertEquals("newuser", createdAccount.getUsername());
                });
    }

    @Test
    void shouldRetrieveAccount() {
        webTestClient.get()
                .uri("/api/v1/accounts/{email}", testEmail)
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDTO.class)
                .consumeWith(response -> {
                    AccountDTO account = response.getResponseBody();
                    assertNotNull(account);
                    assertEquals(testEmail, account.getEmail());
                });
    }

    @Test
    void shouldUpdateAccount() {
        AccountDTO updatedAccount = AccountDTO.builder()
                .email(testEmail)
                .password("newPassword")
                .username("updatedUser")
                .build();

        webTestClient.put()
                .uri("/api/v1/accounts/{email}", testEmail)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedAccount)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountDTO.class)
                .consumeWith(response -> {
                    AccountDTO account = response.getResponseBody();
                    assertNotNull(account);
                    assertEquals("updatedUser", account.getUsername());
                });
    }

    @Test
    void shouldDeleteAccount() {
        webTestClient.delete()
                .uri("/api/v1/accounts/{email}", testEmail)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        assertFalse(accountRepository.existsByEmail(testEmail));
    }

    @Test
    void shouldFailToRetrieveNonexistentAccount() {
        webTestClient.get()
                .uri("/api/v1/accounts/{email}", "nonexistent@example.com")
                .header("Authorization", "Bearer " + userToken)
                .exchange()
                .expectStatus().isNotFound();
    }
}

