package com.microservice.accountService;

import com.microservice.accountService.kafka.producer.AccountEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
public class AccountServiceApplicationTest {

    @MockitoBean
    private AccountEventProducer accountEventProducer;

    @ServiceConnection
    @Container
    static KafkaContainer kafkaContainer =
            new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

    @ServiceConnection
    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("accounts_db")
            .withUsername("test")
            .withPassword("test");

    @Test
    void contextLoads() {
    }
}
