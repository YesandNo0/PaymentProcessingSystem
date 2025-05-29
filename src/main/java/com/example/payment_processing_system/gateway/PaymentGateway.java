package com.example.payment_processing_system.gateway;

import com.example.payment_processing_system.domain.PaymentResponseDTO;
import com.example.payment_processing_system.domain.enums.Status;
import com.example.payment_processing_system.entity.BankCardInfoEntity;
import com.example.payment_processing_system.domain.PaymentRequestDTO;
import com.example.payment_processing_system.repository.BankCardInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentGateway {

    private final BankCardInfoRepository bankCardInfoRepository;

    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        Optional<BankCardInfoEntity> cardInfoOpt = bankCardInfoRepository.findByCardNumber(request.getCardNumber());
        if (!cardInfoOpt.isPresent()) {
            return createErrorResponse("Card not found");
        }
        BankCardInfoEntity cardInfo = cardInfoOpt.get();
        PaymentResponseDTO validationResult = validatePaymentDetails(cardInfo, request);
        if (validationResult != null) {
            return validationResult;
        }
        processTransaction(cardInfo, request.getAmount());
        return PaymentResponseDTO.builder()
                .status(String.valueOf(Status.APPROVED))
                .amount(request.getAmount())
                .build();
    }

    private PaymentResponseDTO validatePaymentDetails(BankCardInfoEntity cardInfo, PaymentRequestDTO request) {
        if (!isValidCVV(cardInfo, request)) {
            return createErrorResponse("Invalid CVV");
        }
        if (!isValidExpiryDate(cardInfo, request)) {
            return createErrorResponse("Invalid expiry date");
        }
        if (!hasSufficientFunds(cardInfo, request.getAmount())) {
            return createErrorResponse("Insufficient funds");
        }
        return null;
    }

    private boolean isValidCVV(BankCardInfoEntity cardInfo, PaymentRequestDTO request) {
        return cardInfo.getCvv() == request.getCvv();
    }

    private boolean isValidExpiryDate(BankCardInfoEntity cardInfo, PaymentRequestDTO request) {
        if (cardInfo.getCardExpiryDate() == null || request.getCardExpiryDate() == null) {
            return false;
        }
        return cardInfo.getCardExpiryDate().trim().equals(request.getCardExpiryDate().trim());
    }

    private boolean hasSufficientFunds(BankCardInfoEntity cardInfo, double requestedAmount) {
        BigDecimal amount = BigDecimal.valueOf(requestedAmount);
        return cardInfo.getBalance().compareTo(amount) >= 0;
    }

    private void processTransaction(BankCardInfoEntity cardInfo, double amount) {
        cardInfo.setBalance(cardInfo.getBalance().subtract(BigDecimal.valueOf(amount)));
        bankCardInfoRepository.save(cardInfo);
    }

    private PaymentResponseDTO createErrorResponse(String errorMessage) {
        return PaymentResponseDTO.builder()
                .status(String.valueOf(Status.DECLINED))
                .error(errorMessage)
                .build();
    }
}
