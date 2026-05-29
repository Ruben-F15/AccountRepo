package com.microservice.accountService.kafka.event;

public record FundsCreditFailedEvent(
        String transactionId,
        String failReason,
        String sourceUserId
) {}
