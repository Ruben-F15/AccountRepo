package com.microservice.accountService.kafka.event;

import java.math.BigDecimal;

public record FundsReservedEvent(
        String sourceUserId,
        String transactionId // Se añade un ID para rastrear la saga// mejor añadirlo en MDC - HEADER KAFKA????
) {}
