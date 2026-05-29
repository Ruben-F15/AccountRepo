package com.microservice.accountService.integration;

import com.microservice.accountService.domain.AccountDocument;
import com.microservice.accountService.domain.TransactionOperation;
import com.microservice.accountService.domain.enums.TransactionOperationStatus;
import com.microservice.accountService.kafka.event.TransferRequestedEvent;
import com.microservice.accountService.kafka.producer.AccountEventProducer;
import com.microservice.accountService.repository.AccountRepository;
import com.microservice.accountService.repository.TransactionOperationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;


import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class TransferKafkaIntegrationTest {

    @ServiceConnection
    @Container
    static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @ServiceConnection
    @Container
    static MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0")
            .withDatabaseName("accounts_db")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private AccountEventProducer accountEventProducer;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionOperationRepository transactionOperationRepository;

    @Test
    void shouldReserveFundsWhenTransferRequestedEventArrives() {
        AccountDocument account = AccountDocument.builder()
                .userId("user-1")
                .accountBalance(new BigDecimal("100.00"))
                .availableAmount(new BigDecimal("100.00"))
                .reservedAmount(BigDecimal.ZERO)
                .accountNumber("ACC-01")
                .status("ACTIVE")
                .openedAt(Instant.now())
                .build();

        accountRepository.save(account);

        TransferRequestedEvent event = new TransferRequestedEvent(
                "user-1",
                new BigDecimal("50.00"),
                "tx-1"
        );

        kafkaTemplate.send("transfer.requested", "user-1", event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            AccountDocument updated = accountRepository.findByUserId("user-1").orElseThrow();

            assertEquals(new BigDecimal("50.00"), updated.getAvailableAmount());
            assertEquals(new BigDecimal("50.00"), updated.getReservedAmount());

            TransactionOperation operation = transactionOperationRepository.findById("tx-1").orElseThrow();

            assertEquals(TransactionOperationStatus.RESERVED, operation.getStatus());

        });
    }
}
