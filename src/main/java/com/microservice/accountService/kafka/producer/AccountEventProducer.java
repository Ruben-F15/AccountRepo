package com.microservice.accountService.kafka.producer;

import com.microservice.accountService.kafka.event.FundsCreditedEvent;
import com.microservice.accountService.kafka.event.FundsDebitedEvent;
import com.microservice.accountService.kafka.event.FundsReservationFailedEvent;
import com.microservice.accountService.kafka.event.FundsReservedEvent;
import com.microservice.accountService.kafka.topics.KafkaTopics;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class AccountEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendFundsReservedEvent(FundsReservedEvent event) {
        String correlationId = MDC.get("correlationId");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.TRANSFER_FUNDS_RESERVED.getTopic(),
                event.sourceUserId(),
                event
        );

        if (correlationId != null) { // enviamos en header el correlationalId para tracking.
            record.headers().add("correlationId", correlationId.getBytes());
        }

        kafkaTemplate.send(record);
    }

    public void sendFundsReservationFailedEvent(FundsReservationFailedEvent event) {
        String correlationId = MDC.get("correlationId");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.TRANSFER_FUNDS_RESERVATION_FAILED.getTopic(),
                event.sourceUserId(),
                event
        );

        if (correlationId != null) { // enviamos en header el correlationalId para tracking.
            record.headers().add("correlationId", correlationId.getBytes());
        }

        kafkaTemplate.send(record);
    }

    public void sendFundsDebitedEvent(FundsDebitedEvent event) {
        String correlationId = MDC.get("correlationId");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.TRANSFER_FUNDS_DEBITED.getTopic(),
                event.sourceUserId(),
                event
        );

        if (correlationId != null) {
            record.headers().add("correlationId", correlationId.getBytes());
        }

        kafkaTemplate.send(record);
    }

    public void sendFundsCreditedEvent(FundsCreditedEvent event) {
        String correlationId = MDC.get("correlationId");

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.TRANSFER_FUNDS_CREDITED.getTopic(),
                event.sourceUserId(),
                event
        );

        if (correlationId != null) {
            record.headers().add("correlationId", correlationId.getBytes());
        }

        kafkaTemplate.send(record);
    }
}
