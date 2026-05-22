package com.microservice.accountService.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {

        // DEFINIMOS QUE HACER CUANDO NO QUEDAN RETRIES
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                //RECORD DEL MENSAJE ORIGINAL DEL EVENTO QUE ESTÁ FALLANDO Y LA EXCEPTION QUE SALTA
                (record, exception) -> {
                    // TRAZABILIDAD CON LOG.
                    log.error("Sending message to DLT. topic= {}, key={}, error={}",
                            record.topic(),
                            record.key(),
                            exception.getMessage()
                    );
                    // CREAMOS TOPIC DLT CON EL NOMNRE DEL TOPIC INICIAL.DLT, "ESTANDAR", MANTENEMOS LA PARTICION PARA ORDERING
                    return new org.apache.kafka.common.TopicPartition(record.topic() + ".DLT", record.partition());
                }
        );

        // politica de retries
        FixedBackOff fixedBackOff = new FixedBackOff(
                3000L, //intento cada 3 segundos
                3 //3 retries maximos (Total 4 intentos)
        );

        //Definimos el errorHandler y le decimos que use el recoverer configurado y la politica de retries
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer, fixedBackOff
        );

        // aquí pondremos las posibles excepciones que sean irrecuperables de cara al evento.
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class
        );

        return errorHandler;
    }






}
