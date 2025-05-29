package com.example.payment_processing_system.controller;

import com.example.payment_processing_system.PaymentProcessingSystemApplication;
import com.example.payment_processing_system.config.TestSecurityConfig;
import com.example.payment_processing_system.domain.BankCardInfoDTO;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.entity.BankCardInfoEntity;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.BankCardInfoRepository;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BankCardInfoRestControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BankCardInfoRepository bankCardInfoRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String adminToken;
    private UUID adminId;
    private String testCardNumber;

    @BeforeEach
    @Transactional
    void setup() {
        bankCardInfoRepository.deleteAll();
        accountRepository.deleteAll();

        AccountEntity user = accountRepository.save(AccountEntity.builder()
                .email("admin@example.com")
                .passwordHash("password")
                .username("admin")
                .role(Role.ADMIN)
                .build());
        testCardNumber = "5555111122223333";
        adminId = user.getId();
        adminToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole());

        bankCardInfoRepository.save(BankCardInfoEntity.builder()
                .cardNumber(testCardNumber)
                .cardExpiryDate("12/26")
                .balance(BigDecimal.valueOf(1000))
                .account(user)
                .cvv(300)
                .build());

    }

    @Test
    void shouldAddBankCard() {
        BankCardInfoDTO newCard = BankCardInfoDTO.builder()
                .cardNumber("4444333322221111")
                .cardExpiryDate("01/28")
                .balance(BigDecimal.valueOf(500))
                .cvv(123)
                .build();

        webTestClient.post()
                .uri("/api/v1/bank-cards/add")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(newCard)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BankCardInfoDTO.class)
                .consumeWith(response -> {
                    BankCardInfoDTO createdCard = response.getResponseBody();
                    assertNotNull(createdCard);
                    assertEquals("4444333322221111", createdCard.getCardNumber());
                });
    }

    @Test
    void shouldRetrieveBankCard() {
        webTestClient.get()
                .uri("/api/v1/bank-cards/{cardNumber}", testCardNumber)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankCardInfoDTO.class)
                .consumeWith(response -> {
                    BankCardInfoDTO card = response.getResponseBody();
                    assertNotNull(card);
                    assertEquals(testCardNumber, card.getCardNumber());
                });
    }

    @Test
    void shouldUpdateBankCard() {
        BankCardInfoDTO updatedCard = BankCardInfoDTO.builder()
                .cardNumber(testCardNumber)
                .cardExpiryDate("01/27")
                .cvv(300)
                .balance(BigDecimal.valueOf(1500))
                .build();

        webTestClient.put()
                .uri("/api/v1/bank-cards/{cardNumber}", testCardNumber)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCard)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BankCardInfoDTO.class)
                .consumeWith(response -> {
                    BankCardInfoDTO card = response.getResponseBody();
                    assertNotNull(card);
                    assertEquals("01/27", card.getCardExpiryDate());
                });
    }

    @Test
    void shouldDeleteBankCard() {
        webTestClient.delete()
                .uri("/api/v1/bank-cards/{cardNumber}", testCardNumber)
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNoContent();

        assertTrue(bankCardInfoRepository.findByCardNumber(testCardNumber).isEmpty());
    }

    @Test
    void shouldFailToRetrieveNonexistentBankCard() {
        webTestClient.get()
                .uri("/api/v1/bank-cards/{cardNumber}", "0000111122223333")
                .header("Authorization", "Bearer " + adminToken)
                .exchange()
                .expectStatus().isNotFound();
    }
}
