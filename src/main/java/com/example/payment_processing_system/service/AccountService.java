package com.example.payment_processing_system.service;

import com.example.payment_processing_system.domain.AccountDTO;

public interface AccountService {
    AccountDTO createAccount(AccountDTO accountDTO);
    AccountDTO updateAccount(String email, AccountDTO accountDTO);
    void deleteAccount(String email);
    AccountDTO getAccountByEmail(String email);
}
