package com.example.payment_processing_system.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import com.example.payment_processing_system.domain.SubscriptionDTO;
import com.example.payment_processing_system.service.SubscriptionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "APIs for managing user subscriptions")
public class SubscriptionRestController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    @Operation(summary = "Create subscription", description = "Creates a new subscription for a user")
    @ApiResponse(responseCode = "200", description = "Subscription created successfully")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public SubscriptionDTO createSubscription(@RequestParam String userEmail, @RequestBody SubscriptionDTO subscriptionDTO) {
        return subscriptionService.createSubscription(userEmail, subscriptionDTO);
    }

    @GetMapping("/user")
    @Operation(summary = "Get user subscriptions", description = "Retrieves all subscriptions for a specific user")
    @ApiResponse(responseCode = "200", description = "Subscriptions retrieved successfully")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<SubscriptionDTO> getUserSubscriptions(@RequestParam String userEmail) {
        return subscriptionService.getUserSubscriptions(userEmail);
    }

    @PutMapping("/cancel/{subscriptionId}")
    @Operation(summary = "Cancel subscription", description = "Cancels a user's subscription")
    @ApiResponse(responseCode = "200", description = "Subscription canceled successfully")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public SubscriptionDTO cancelSubscription(@PathVariable UUID subscriptionId) {
        return subscriptionService.cancelSubscription(subscriptionId);
    }
}
