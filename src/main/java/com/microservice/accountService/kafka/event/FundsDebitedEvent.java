package com.microservice.accountService.kafka.event;

import java.math.BigDecimal;

public record FundsDebitedEvent(
        String sourceUserId,
        String transactionId
) {
}
