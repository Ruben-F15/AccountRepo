package com.microservice.accountService.kafka.consumer;

import com.microservice.accountService.kafka.event.TransferDebitRequestedEvent;
import com.microservice.accountService.kafka.event.TransferRequestedEvent;
import com.microservice.accountService.service.AccountTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferEventListener {

    private final AccountTransferService accountTransferService;

    @KafkaListener(topics = "transfer.requested")
    public void handleTransferRequested(TransferRequestedEvent event,
                                        @Header(value = "correlationId", required = false) String correlationId) {
        try {
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            log.info("Received TransferRequested event for userId= {}", event.sourceUserId());

            accountTransferService.handleTransferRequest(event);
        } finally {
            MDC.clear();
        }
    }

    @KafkaListener(topics = "transfer.debit.requested")
    public void handleTransferDebitRequestedEvent(TransferDebitRequestedEvent debitRequestedEvent,
                                                  @Header(value = "correlationId", required = false) String correlationId) {
        try {
            if (correlationId != null) {
                MDC.put("correlationId", correlationId);
            }

            log.info("Received TransferDebitRequestedEvent event for userId= {}", debitRequestedEvent.sourceUserId());

            accountTransferService.handleTransferDebitRequest(debitRequestedEvent);
        } finally {
            MDC.clear();
        }

    }
}
