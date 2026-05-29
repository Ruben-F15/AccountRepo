package com.microservice.accountService.kafka.event;

import lombok.Getter;

@Getter
public enum EventErrors {
    INSUFFICIENT_FUNDS("insufficent.funds"),
    INVALID_AMOUNT("invalid.amount"),
    ACCOUNT_NOT_FOUND("account.not.found"),
    UNDEFINED_ERROR("undefined.error.handle.transfer.request"),
    EVENT_PUBLISH_FAILED("event.publish.failed"),
    TECHNICAL_FAILURE("technical.failure"),
    PROCESSING_FAILED("processing.failed"),
    UNEXPECTED_SYSTEM_ERROR("unexpected.system.error");

    private final String error;
    EventErrors(String error) {
        this.error = error;
    }

}
