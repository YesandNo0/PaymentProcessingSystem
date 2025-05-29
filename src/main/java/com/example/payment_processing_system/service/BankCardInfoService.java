package com.example.payment_processing_system.service;

import com.example.payment_processing_system.domain.BankCardInfoDTO;

public interface BankCardInfoService {
    BankCardInfoDTO addBankCard(BankCardInfoDTO bankCardInfoDTO);
    BankCardInfoDTO getBankCardByNumber(String cardNumber);
    BankCardInfoDTO updateBankCard(String cardNumber, BankCardInfoDTO bankCardInfoDTO);
    void deleteBankCard(String cardNumber);
}
