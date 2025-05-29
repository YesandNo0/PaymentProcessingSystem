package com.example.payment_processing_system.service.impl;

import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.exception.AccountNotFoundException;
import com.example.payment_processing_system.domain.AccountDTO;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccountDTO createAccount(AccountDTO accountDTO) {
        if (accountRepository.existsByEmail(accountDTO.getEmail())) {
            throw new RuntimeException("Account already exists with email: " + accountDTO.getEmail());
        }

        String hashedPassword = passwordEncoder.encode(accountDTO.getPassword());

        AccountEntity entity = AccountEntity.builder()
                .email(accountDTO.getEmail())
                .username(accountDTO.getUsername())
                .role(Role.USER)
                .passwordHash(hashedPassword)
                .build();

        AccountEntity savedEntity = accountRepository.save(entity);
        return mapToDTO(savedEntity);
    }

    @Override
    public AccountDTO updateAccount(String email, AccountDTO accountDTO) {
        AccountEntity entity = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with email: " + email));

        entity.setUsername(accountDTO.getUsername());
        entity.setRole(Role.USER);

        if (accountDTO.getPassword() != null && !accountDTO.getPassword().isBlank()) {
            entity.setPasswordHash(passwordEncoder.encode(accountDTO.getPassword()));
        }

        AccountEntity updatedEntity = accountRepository.save(entity);
        return mapToDTO(updatedEntity);
    }

    @Override
    public void deleteAccount(String email) {
        if (!accountRepository.existsByEmail(email)) {
            throw new AccountNotFoundException("Account not found with email: " + email);
        }
        accountRepository.deleteByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDTO getAccountByEmail(String email) {
        AccountEntity account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with email: " + email));

        return mapToDTO(account);
    }

    private AccountDTO mapToDTO(AccountEntity entity) {
        return AccountDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .build();
    }
}
