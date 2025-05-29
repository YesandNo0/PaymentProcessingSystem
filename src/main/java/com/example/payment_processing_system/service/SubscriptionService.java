package com.example.payment_processing_system.service;

import com.example.payment_processing_system.domain.SubscriptionDTO;
import java.util.List;
import java.util.UUID;

public interface SubscriptionService {
    SubscriptionDTO createSubscription(String userEmail, SubscriptionDTO subscriptionDTO);
    List<SubscriptionDTO> getUserSubscriptions(String userEmail);
    SubscriptionDTO cancelSubscription(UUID subscriptionId);
}
