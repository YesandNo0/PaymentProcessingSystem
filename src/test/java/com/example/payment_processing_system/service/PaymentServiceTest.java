package com.example.payment_processing_system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.entity.BankCardInfoEntity;
import com.example.payment_processing_system.entity.PaymentEntity;
import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.domain.enums.Status;
import com.example.payment_processing_system.exception.AccountNotFoundException;
import com.example.payment_processing_system.gateway.PaymentGateway;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.BankCardInfoRepository;
import com.example.payment_processing_system.repository.PaymentRepository;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.service.impl.PaymentServiceImpl;
import com.example.payment_processing_system.validation.PaymentValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentValidator paymentValidator;
    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BankCardInfoRepository bankCardInfoRepository;
    @Mock
    private EmailService emailService;

    private PaymentServiceImpl paymentService;
    private PaymentRequestDTO defaultRequest;
    private PaymentResponseDTO defaultResponse;
    private AccountEntity defaultAccount;
    private BankCardInfoEntity defaultCardInfo;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentValidator, paymentGateway, accountRepository, paymentRepository, emailService
        );

        defaultRequest = PaymentRequestDTO.builder()
                .email("user@example.com")
                .cardNumber("CARD123")
                .cardExpiryDate("12/25")
                .cvv(123)
                .amount(150.0)
                .build();

        defaultResponse = PaymentResponseDTO.builder()
                .transactionId(UUID.randomUUID())
                .status(String.valueOf(Status.APPROVED))
                .amount(150.0)
                .error(null)
                .build();

        defaultAccount = AccountEntity.builder()
                .email("user@example.com")
                .username("Test User")
                .role(Role.USER)
                .build();

        defaultCardInfo = BankCardInfoEntity.builder()
                .cardNumber("CARD123")
                .balance(BigDecimal.valueOf(1000.0))
                .cardExpiryDate("12/25")
                .cvv(123)
                .build();
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        when(paymentGateway.processPayment(defaultRequest)).thenReturn(defaultResponse);
        when(accountRepository.findByEmail(defaultAccount.getEmail())).thenReturn(Optional.of(defaultAccount));
        when(paymentRepository.save(any(PaymentEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDTO result = paymentService.processPayment(defaultRequest);

        assertEquals(defaultResponse.getAmount(), result.getAmount());
        assertEquals(defaultResponse.getStatus(), result.getStatus());
        verify(paymentValidator).validatePayment(defaultRequest);
        verify(paymentGateway).processPayment(defaultRequest);

        ArgumentCaptor<PaymentEntity> captor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(captor.capture());
        PaymentEntity savedEntity = captor.getValue();

        assertEquals(defaultAccount, savedEntity.getAccount());
        assertEquals(BigDecimal.valueOf(defaultRequest.getAmount()), savedEntity.getAmount());
        assertEquals(String.valueOf(defaultResponse.getStatus()), savedEntity.getStatus());
        assertNotNull(savedEntity.getTimestamp());

        verify(emailService).sendEmail(eq(defaultAccount.getEmail()), anyString(), anyString());
    }


    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () ->
                paymentService.processPayment(defaultRequest));
        assertEquals("Account not found", exception.getMessage());

        verify(paymentRepository, never()).save(any(PaymentEntity.class));
    }
}
