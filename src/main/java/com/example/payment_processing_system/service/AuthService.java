package com.example.payment_processing_system.service;

import com.example.payment_processing_system.domain.AccountDTO;
import com.example.payment_processing_system.domain.auth.LoginRequest;
import com.example.payment_processing_system.domain.auth.RegisterRequest;

public interface AuthService {
    AccountDTO loginAccount(LoginRequest request);
    AccountDTO registerAccount(RegisterRequest request);
    String generateToken(AccountDTO user);
}
