package com.example.payment_processing_system.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.exception.PaymentValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PaymentValidatorTest {

    private PaymentValidator validator;
    private PaymentRequestDTO validRequest;

    @BeforeEach
    void setUp() {
        validator = new PaymentValidator();
        validRequest = PaymentRequestDTO.builder()
                .cardNumber("4111111111111111")
                .cardExpiryDate("12/99")
                .cvv(123)
                .amount(100.0)
                .build();
    }

    @Test
    void validatePayment_withValidInput_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validatePayment(validRequest));
    }

    @Test
    void validatePayment_withEmptyCardNumber_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("   ")
                .cardExpiryDate("12/99")
                .cvv(123)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Card number cannot be empty", ex.getMessage());
    }

    @Test
    void validatePayment_withInvalidCardNumberLength_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("411111111111")
                .cardExpiryDate("12/99")
                .cvv(123)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Card number must be between 13 and 19 digits", ex.getMessage());
    }

    @Test
    void validatePayment_withUnsupportedCardType_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("9111111111111111")
                .cardExpiryDate("12/99")
                .cvv(123)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Unsupported card type", ex.getMessage());
    }

    @Test
    void validatePayment_withInvalidLuhn_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("4111111111111112")
                .cardExpiryDate("12/99")
                .cvv(123)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Invalid card number", ex.getMessage());
    }

    @Test
    void validatePayment_withExpiredCard_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("4111111111111111")
                .cardExpiryDate("01/20")
                .cvv(123)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Card has expired", ex.getMessage());
    }

    @Test
    void validatePayment_withInvalidExpiryFormat_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("4111111111111111")
                .cardExpiryDate("invalid")
                .cvv(123)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Invalid expiry date format (MM/YY)", ex.getMessage());
    }

    @Test
    void validatePayment_withInvalidCVV_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("4111111111111111")
                .cardExpiryDate("12/99")
                .cvv(12)
                .amount(100.0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Invalid CVV", ex.getMessage());
    }

    @Test
    void validatePayment_withNonPositiveAmount_throwsException() {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .cardNumber("4111111111111111")
                .cardExpiryDate("12/99")
                .cvv(123)
                .amount(0)
                .build();
        PaymentValidationException ex = assertThrows(PaymentValidationException.class, () -> validator.validatePayment(req));
        assertEquals("Amount must be positive", ex.getMessage());
    }
}
