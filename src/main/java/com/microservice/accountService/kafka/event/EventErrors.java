package com.microservice.accountService.kafka.event;

import lombok.Getter;

@Getter
public enum EventErrors {
    INSUFFICENT_FUNDS("insufficent.funds"),
    ACCOUNT_NOT_FOUND("account.not.found"),
    UNDEFINED_ERROR("undefined.error.handle.transfer.request");

    private final String error;
    EventErrors(String error) {
        this.error = error;
    }

}
