package com.example.payment_processing_system.service;

public interface EmailService {
    void sendEmail(String to, String subject, String message);
}
