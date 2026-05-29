package com.microservice.accountService.kafka.dlt;

import com.microservice.accountService.kafka.event.*;
import com.microservice.accountService.kafka.producer.AccountEventProducer;
import com.microservice.accountService.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferDltConsumer {

    private final AccountService accountService;
    private final AccountEventProducer accountEventProducer;

    @KafkaListener(topics = "transfer.requested.DLT")
    public void listenReserveDLT(ConsumerRecord<String, TransferRequestedEvent> record) {
        TransferRequestedEvent event = record.value();
        log.error("Message arrived to DLT-listenReserveDLT: {}", event.transactionId());
        log.error(":::::::: ERROR KAFKA RESERVED FUNDS EVENT");
        try {
            accountService.releaseReserveFunds(event.sourceUserId(), event.amount(), event.transactionId());
        } catch (Exception ex) {
            log.error("Compensation failed for transactionId={}", event.transactionId(), ex);
        }

        try {
            accountEventProducer.sendFundsReservationFailedEvent(new FundsReservationFailedEvent(
                            event.transactionId(),
                            EventErrors.TECHNICAL_FAILURE.getError(),
                            event.sourceUserId()
                    )
            );
        } catch (Exception ex) {
            log.error("Failed sending reservation failed event for transactionId={}", event.transactionId(), ex);
        }
    }

    @KafkaListener(topics = "transfer.debit.requested.DLT")
    public void listenDebitDLT(ConsumerRecord<String, TransferDebitRequestedEvent> record) {
        TransferDebitRequestedEvent event = record.value();
        log.error("Message arrived to DLT-listenDebitDLT: {}", event.transactionId());
        log.error(":::::::: ERROR KAFKA DEBIT FUNDS EVENT");

        try {
            accountService.refundAccount(event.sourceUserId(), event.amount(), event.transactionId());
        } catch (Exception ex) {
            log.error("Compensation failed for transactionId={}", event.transactionId(), ex);
        }
        try {
            accountEventProducer.sendFundsDebitFailedEvent(new FundsDebitFailedEvent(
                            event.transactionId(),
                            EventErrors.TECHNICAL_FAILURE.getError(),
                            event.sourceUserId()
                    )
            );
        } catch (Exception ex) {
            log.error("Failed sending debit failed event for transactionId={}", event.transactionId(), ex);
        }
    }

    @KafkaListener(topics = "transfer.credit.requested.DLT")
    public void listenCreditDLT(ConsumerRecord<String, TransferCreditRequestedEvent> record) {
        TransferCreditRequestedEvent event = record.value();
        log.error("Message arrived to DLT-listenCreditDLT: {}", event.transactionId());
        log.error(":::::::: ERROR KAFKA CREDIT FUNDS EVENT");

        try {
            accountService.reverseCredit(event.destinationUserId(), event.amount(), event.transactionId());
            accountService.refundAccount(event.sourceUserId(), event.amount(), event.transactionId());
        } catch (Exception ex) {
            log.error("Compensation failed for transactionId={}", event.transactionId(), ex);
        }
        try {
            accountEventProducer.sendFundsCreditFailedEvent(new FundsCreditFailedEvent(
                            event.transactionId(),
                            EventErrors.TECHNICAL_FAILURE.getError(),
                            event.sourceUserId()
                    )
            );
        } catch (Exception ex) {
            log.error("Failed sending credit failed event for transactionId={}", event.transactionId(), ex);
        }
    }
}
