package com.microservice.accountService.exceptions;

public class AccountServiceException extends RuntimeException {
    public AccountServiceException(String message) {
        super(message);
    }
}
