package com.example.payment_processing_system.validation;

import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.exception.PaymentValidationException;
import org.springframework.stereotype.Service;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
public class PaymentValidator {

    public void validatePayment(PaymentRequestDTO paymentRequest) {
        validateCardNumber(paymentRequest.getCardNumber());
        validateExpiryDate(paymentRequest.getCardExpiryDate());
        validateCVV(paymentRequest.getCvv());
        validateAmount(paymentRequest.getAmount());
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.trim().isEmpty()) {
            throw new PaymentValidationException("Card number cannot be empty");
        }

        String cleanedNumber = cardNumber.replaceAll("[^0-9]", "");

        if (!cleanedNumber.matches("\\d+")) {
            throw new PaymentValidationException("Card number must contain only digits");
        }
        if (cleanedNumber.length() < 13 || cleanedNumber.length() > 19) {
            throw new PaymentValidationException("Card number must be between 13 and 19 digits");
        }
        if (!isValidPaymentSystem(cleanedNumber)) {
            throw new PaymentValidationException("Unsupported card type");
        }
        if (!isValidLuhn(cleanedNumber)) {
            throw new PaymentValidationException("Invalid card number");
        }
    }

    private void validateExpiryDate(String expiryDate) {
        try {
            YearMonth expiry = YearMonth.parse(expiryDate, DateTimeFormatter.ofPattern("MM/yy"));
            if (expiry.isBefore(YearMonth.now())) {
                throw new PaymentValidationException("Card has expired");
            }
        } catch (DateTimeParseException e) {
            throw new PaymentValidationException("Invalid expiry date format (MM/YY)");
        }
    }


    private void validateCVV(int cvv) {
        String cvvStr = String.valueOf(cvv);
        if (cvvStr.length() < 3 || cvvStr.length() > 4) {
            throw new PaymentValidationException("Invalid CVV");
        }
    }

    private void validateAmount(double amount) {
        try {
            if (amount <= 0) {
                throw new PaymentValidationException("Amount must be positive");
            }
        } catch (NumberFormatException e) {
            throw new PaymentValidationException("Invalid amount format");
        }
    }

    private boolean isValidLuhn(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n = (n % 10) + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    private boolean isValidPaymentSystem(String cardNumber) {
        // Visa: starts with 4
        if (cardNumber.startsWith("4")) {
            return true;
        }
        // MasterCard: starts with 5 or 2
        if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) {
            return true;
        }
        // American Express: starts with 34 or 37
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) {
            return true;
        }
        // Maestro: starts with 50, 56-69 or 6
        if (cardNumber.startsWith("50") || (cardNumber.startsWith("5") && cardNumber.charAt(1) >= '6' && cardNumber.charAt(1) <= '9')
                || (cardNumber.startsWith("6"))) {
            return true;
        }
        // UnionPay: starts with 62
        return cardNumber.startsWith("62");
    }
}