package com.microservice.accountService.kafka.topics;

import lombok.Getter;

@Getter
public enum KafkaTopics {
        USER_CREATED("user.created"),
        TRANSFER_REQUESTED("transfer.requested"),
        TRANSFER_FUNDS_RESERVED("transfer.funds.reserved"),
        TRANSFER_FUNDS_RESERVED_FAILED("transfer.funds.reserved.failed"),
        TRANSFER_COMPLETED("transfer.completed"),
        TRANSFER_FAILED("transfer.failed"),
        TRANSFER_ROLLBACK_REQUESTED("transfer.rollback.requested");
        private final String topic;
        KafkaTopics(String topic) {
            this.topic = topic;
        }
}
