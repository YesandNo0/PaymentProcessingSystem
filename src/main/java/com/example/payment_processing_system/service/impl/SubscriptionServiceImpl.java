package com.example.payment_processing_system.service.impl;

import com.example.payment_processing_system.entity.AccountEntity;
import com.example.payment_processing_system.domain.SubscriptionDTO;
import com.example.payment_processing_system.entity.SubscriptionEntity;
import com.example.payment_processing_system.exception.AccountNotFoundException;
import com.example.payment_processing_system.repository.AccountRepository;
import com.example.payment_processing_system.repository.SubscriptionRepository;
import com.example.payment_processing_system.service.EmailService;
import com.example.payment_processing_system.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final AccountRepository accountRepository;
    private final EmailService emailService;

    @Override
    public SubscriptionDTO createSubscription(String userEmail, SubscriptionDTO subscriptionDTO) {
        Optional<AccountEntity> accountOpt = accountRepository.findByEmail(userEmail);
        if (accountOpt.isEmpty()) {
            throw new AccountNotFoundException("User account not found for email: " + userEmail);
        }
        AccountEntity account = accountOpt.get();

        SubscriptionEntity entity = SubscriptionEntity.builder()
                .account(account)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .amount(subscriptionDTO.getAmount())
                .status("ACTIVE")
                .build();

        SubscriptionEntity savedEntity = subscriptionRepository.save(entity);

        sendSubscriptionConfirmationEmail(userEmail, savedEntity);

        return mapToDTO(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getUserSubscriptions(String userEmail) {
        List<SubscriptionEntity> subscriptions = subscriptionRepository.findByAccountEmail(userEmail);
        return subscriptions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public SubscriptionDTO cancelSubscription(UUID subscriptionId) {
        SubscriptionEntity subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));

        subscription.setStatus("CANCELLED");
        subscription.setEndDate(LocalDate.now());
        SubscriptionEntity updatedSubscription = subscriptionRepository.save(subscription);
        sendSubscriptionCancellationEmail(subscription.getAccount().getEmail(), updatedSubscription);

        return mapToDTO(updatedSubscription);
    }

    private SubscriptionDTO mapToDTO(SubscriptionEntity entity) {
        return SubscriptionDTO.builder()
                .subscriptionId(entity.getSubscriptionId())
                .accountId(entity.getAccount().getId())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .build();
    }

    private void sendSubscriptionConfirmationEmail(String userEmail, SubscriptionEntity subscription) {
        String subject = "Subscription Activated";
        String message = String.format(
                "Your subscription has been successfully activated.\nStart Date: %s\nEnd Date: %s\nStatus: %s",
                subscription.getStartDate(), subscription.getEndDate(), subscription.getStatus());

        emailService.sendEmail(userEmail, subject, message);
    }

    private void sendSubscriptionCancellationEmail(String userEmail, SubscriptionEntity subscription) {
        String subject = "Subscription Cancelled";
        String message = String.format(
                "Your subscription has been cancelled.\nEnd Date: %s\nStatus: %s\nAmount: %.2f",
                subscription.getEndDate(), subscription.getStatus(), subscription.getAmount());

        emailService.sendEmail(userEmail, subject, message);
    }
}
