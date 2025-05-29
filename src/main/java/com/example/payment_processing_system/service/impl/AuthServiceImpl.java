package com.example.payment_processing_system.service.impl;

import com.example.payment_processing_system.domain.AccountDTO;
import com.example.payment_processing_system.domain.auth.LoginRequest;
import com.example.payment_processing_system.domain.auth.RegisterRequest;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.exception.AccountAlreadyExistsException;
import com.example.payment_processing_system.exception.AuthenticationException;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.security.jwt.JwtTokenProvider;
import com.example.payment_processing_system.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccountDTO registerAccount(RegisterRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new AccountAlreadyExistsException("Account with email " + request.getEmail() + " already exists");
        }

        AccountEntity account = accountRepository.save(AccountEntity.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build());

        return AccountDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .build();
    }

    @Override
    public AccountDTO loginAccount(LoginRequest request) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(request.getEmail());
        AccountEntity account = accountOpt.orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            System.out.println("Password does not match for email: " + request.getEmail());
            throw new AuthenticationException("Invalid email or password");
        }

        return AccountDTO.builder()
                .id(account.getId())
                .email(account.getEmail())
                .username(account.getUsername())
                .build();
    }

    public String generateToken(AccountDTO account) {
        Role role = accountRepository.findByEmail(account.getEmail()).get().getRole();
        return jwtTokenProvider.createToken(account.getEmail(), role);
    }
}
