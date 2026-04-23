package com.microservice.accountService.exceptions;

public class AccountException extends RuntimeException {
    public AccountException(String message) {
        super("La cuenta "message);
    }
}
