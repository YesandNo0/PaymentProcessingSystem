package com.example.payment_processing_system.service;

import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.domain.SubscriptionDTO;
import com.example.payment_processing_system.entity.SubscriptionEntity;
import com.example.payment_processing_system.exception.AccountNotFoundException;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.SubscriptionRepository;
import com.example.payment_processing_system.domain.enums.Role;
import com.example.payment_processing_system.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private AccountEntity defaultAccount;
    private SubscriptionDTO defaultSubscriptionDTO;

    @BeforeEach
    void setUp() {
        defaultAccount = AccountEntity.builder()
                .email("user@example.com")
                .username("John Doe")
                .role(Role.USER)
                .passwordHash("hashed_password")
                .build();

        defaultSubscriptionDTO = SubscriptionDTO.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status("ACTIVE")
                .build();
    }

    @Test
    void shouldCreateSubscriptionSuccessfully() {
        when(accountRepository.findByEmail("user@example.com")).thenReturn(Optional.of(defaultAccount));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionDTO result = subscriptionService.createSubscription("user@example.com", defaultSubscriptionDTO);

        assertEquals("ACTIVE", result.getStatus());
        assertEquals(LocalDate.now(), result.getStartDate());

        verify(emailService).sendEmail(eq("user@example.com"), anyString(), anyString());
        verify(subscriptionRepository).save(any(SubscriptionEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(accountRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                subscriptionService.createSubscription("unknown@example.com", defaultSubscriptionDTO));

        verify(subscriptionRepository, never()).save(any(SubscriptionEntity.class));
    }

    @Test
    void shouldCancelSubscriptionSuccessfully() {
        SubscriptionEntity subscription = SubscriptionEntity.builder()
                .subscriptionId(UUID.randomUUID())
                .account(defaultAccount)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .status("ACTIVE")
                .build();

        when(subscriptionRepository.findById(subscription.getSubscriptionId())).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(SubscriptionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubscriptionDTO result = subscriptionService.cancelSubscription(subscription.getSubscriptionId());

        assertEquals("CANCELLED", result.getStatus());
        assertEquals(LocalDate.now(), result.getEndDate());

        verify(emailService).sendEmail(eq("user@example.com"), anyString(), anyString());
        verify(subscriptionRepository).save(any(SubscriptionEntity.class));
    }
}
