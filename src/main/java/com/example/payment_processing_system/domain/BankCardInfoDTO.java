package com.example.payment_processing_system.domain;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder(toBuilder = true)
public class BankCardInfoDTO {
    String cardNumber;
    BigDecimal balance;
    String cardExpiryDate;
    int cvv;
}