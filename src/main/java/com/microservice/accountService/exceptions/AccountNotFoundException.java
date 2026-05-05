package com.microservice.accountService.exceptions;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message, String accountId) {
        super("La cuenta con ID: " + accountId + "no existe. " + message);
    }
}
