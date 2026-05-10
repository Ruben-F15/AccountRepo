package com.microservice.accountService.kafka.dlt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserCreatedDltConsumer {

    @KafkaListener(topics = "user.created.DLT")
    public void listenDLT(String message) {
        log.error("Message arrived to DLT: {}",
                message);

        // IMPLEMENTAR EN FUTURO:
        // - guardar en DB
        // - enviar alerta
        // - reprocesar
        // - notificar monitoring
    }
}
