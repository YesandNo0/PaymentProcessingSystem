package com.example.payment_processing_system.domain;

import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class AccountDTO {
    UUID id;
    String email;
    String username;
    String password;
}