package com.microservice.accountService.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super("Fondos insuficientes. " + message);
    }
}
