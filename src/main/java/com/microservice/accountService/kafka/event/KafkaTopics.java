package com.microservice.accountService.kafka.event;

import lombok.Getter;

@Getter
public enum KafkaTopics {
        USER_CREATED("user.created"),
        TRANSFER_REQUESTED("transfer.requested"),
        TRANSFER_COMPLETED("transfer.completed");

        private final String topic;

        KafkaTopics(String topic) {
            this.topic = topic;
        }
}
