package com.microservice.accountService.kafka.topics;

import lombok.Getter;

@Getter
public enum KafkaTopics {
        USER_CREATED("user.created"),

        TRANSFER_REQUESTED("transfer.requested"),
        TRANSFER_FUNDS_RESERVED("transfer.funds.reserved"),
        TRANSFER_FUNDS_RESERVATION_FAILED("transfer.funds.reservation.failed"),
        TRANSFER_DEBIT_REQUESTED("transfer.debit.requested"),
        TRANSFER_FUNDS_DEBITED("transfer.funds.debited"),
        TRANSFER_CREDIT_REQUESTED("transfer.credit.requested"),
        TRANSFER_COMPLETED("transfer.completed"),
        TRANSFER_FAILED("transfer.failed"),
        TRANSFER_ROLLBACK_REQUESTED("transfer.rollback.requested");
        private final String topic;
        KafkaTopics(String topic) {
            this.topic = topic;
        }
}
