package com.example.payment_processing_system.gateway;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.payment_processing_system.entity.BankCardInfoEntity;
import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.domain.enums.Status;
import com.example.payment_processing_system.repository.BankCardInfoRepository;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PaymentGatewayTest {

    @Mock
    private BankCardInfoRepository bankCardInfoRepository;

    private PaymentGateway paymentGateway;
    private PaymentRequestDTO defaultRequest;
    private BankCardInfoEntity defaultCardInfo;

    @BeforeEach
    void setUp() {
        paymentGateway = new PaymentGateway(bankCardInfoRepository);
        defaultRequest = PaymentRequestDTO.builder()
                .cardNumber("CARD123")
                .cardExpiryDate("12/25")
                .cvv(123)
                .amount(300.0)
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
        when(bankCardInfoRepository.findByCardNumber("CARD123")).thenReturn(Optional.of(defaultCardInfo));
        when(bankCardInfoRepository.save(any(BankCardInfoEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponseDTO result = paymentGateway.processPayment(defaultRequest);
        assertEquals(Status.APPROVED, Status.valueOf(result.getStatus()));
        assertNull(result.getError());

        verify(bankCardInfoRepository).save(any(BankCardInfoEntity.class));
    }

    @Test
    void shouldReturnDeclinedWhenCardNotFound() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .cardNumber("CARD999")
                .cardExpiryDate("12/25")
                .cvv(123)
                .amount(150.0)
                .build();
        when(bankCardInfoRepository.findByCardNumber("CARD999")).thenReturn(Optional.empty());

        PaymentResponseDTO result = paymentGateway.processPayment(request);
        assertEquals(Status.DECLINED, Status.valueOf(result.getStatus()));
        assertEquals("Card not found", result.getError());

        verify(bankCardInfoRepository, never()).save(any(BankCardInfoEntity.class));
    }

    @Test
    void shouldReturnDeclinedWhenInsufficientFunds() {
        BankCardInfoEntity lowBalanceCard = BankCardInfoEntity.builder()
                .cardNumber("CARD123")
                .balance(BigDecimal.valueOf(100.0))
                .cardExpiryDate("12/25")
                .cvv(123)
                .build();
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .cardNumber("CARD123")
                .cardExpiryDate("12/25")
                .cvv(123)
                .amount(150.0)
                .build();
        when(bankCardInfoRepository.findByCardNumber("CARD123")).thenReturn(Optional.of(lowBalanceCard));

        PaymentResponseDTO result = paymentGateway.processPayment(request);
        assertEquals(Status.DECLINED, Status.valueOf(result.getStatus()));
        assertEquals("Insufficient funds", result.getError());
    }

    @Test
    void shouldReturnDeclinedWhenInvalidCVV() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .cardNumber("CARD123")
                .cardExpiryDate("12/25")
                .cvv(999)
                .amount(300.0)
                .build();
        when(bankCardInfoRepository.findByCardNumber("CARD123")).thenReturn(Optional.of(defaultCardInfo));

        PaymentResponseDTO result = paymentGateway.processPayment(request);
        assertEquals(Status.DECLINED, Status.valueOf(result.getStatus()));
        assertEquals("Invalid CVV", result.getError());
    }

    @Test
    void shouldReturnDeclinedWhenInvalidExpiryDate() {
        PaymentRequestDTO request = PaymentRequestDTO.builder()
                .cardNumber("CARD123")
                .cardExpiryDate("11/25")
                .cvv(123)
                .amount(300.0)
                .build();
        when(bankCardInfoRepository.findByCardNumber("CARD123")).thenReturn(Optional.of(defaultCardInfo));

        PaymentResponseDTO result = paymentGateway.processPayment(request);
        assertEquals(Status.DECLINED, Status.valueOf(result.getStatus()));
        assertEquals("Invalid expiry date", result.getError());
    }
}
