package com.microservice.accountService.kafka.event;

public record FundsCreditedEvent(
        String sourceUserId,
        String transactionId
) {
}
