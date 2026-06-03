package com.microservice.accountService.integration;

import com.microservice.accountService.kafka.event.TransferRequestedEvent;
import com.microservice.accountService.kafka.producer.AccountEventProducer;
import com.microservice.accountService.service.AccountService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
public class TransferKafkaDLTIntegrationTest {

    @ServiceConnection
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer(
                    DockerImageName.parse("apache/kafka-native:3.8.0"));

    @ServiceConnection
    @Container
    static MySQLContainer mysqlContainer = new MySQLContainer("mysql:8.0")
                    .withDatabaseName("accounts_db_test")
                    .withUsername("test")
                    .withPassword("test");

    @AfterAll
    static void tearDown() {
        kafkaContainer.stop();
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private AccountEventProducer accountEventProducer;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    DataSource dataSource;

    @Test
    void debugDatasource() throws Exception {
        System.out.println(
                dataSource.getConnection()
                        .getMetaData()
                        .getURL()
        );
    }

    @Test
    void shouldSendMessageToDLTAfterRetries() {

        doThrow(new RuntimeException("DB DOWN")).when(accountService).reserveFunds(any(), any(), any());

        TransferRequestedEvent event = new TransferRequestedEvent(
                        "user-1",
                        new BigDecimal("50.00"),
                        "tx-dlt-1"
                );

        kafkaTemplate.send("transfer.requested", "user-1", event);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() -> verify(accountEventProducer).sendFundsReservationFailedEvent(
                argThat(failedEvent -> failedEvent.transactionId().equals("tx-dlt-1"))
        ));
    }


}
