package com.example.payment_processing_system.exception;

public class BankCardAlreadyExistsException extends RuntimeException {
    public BankCardAlreadyExistsException(String message) {
        super(message);
    }
}
