package com.microservice.accountService.kafka.event;

public record UserCreatedEvent(
        String userId
) {}
