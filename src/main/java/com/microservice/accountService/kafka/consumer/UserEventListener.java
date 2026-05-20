package com.microservice.accountService.kafka.consumer;

import com.microservice.accountService.kafka.event.UserCreatedEvent;
import com.microservice.accountService.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final AccountService accountService;

    @KafkaListener(topics = "user.created")
    public void handleUserCreated(UserCreatedEvent event, @Header(value = "correlationId", required = false ) String correlationId) {
        try {
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            log.info("Received UserCreated event for userId= {}", event.userId());

            accountService.createAccount(event.userId());

        } finally {
            MDC.clear();
        }
    }
}
