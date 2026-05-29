package com.microservice.accountService.domain;

import com.microservice.accountService.domain.enums.TransactionOperationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "transaction_operation")
@NoArgsConstructor
@AllArgsConstructor
public class TransactionOperation {
    @Id
    private String transactionId;

    @Enumerated(EnumType.STRING)
    private TransactionOperationStatus status;

    private Instant processedAt;

    public void updateStatus(TransactionOperationStatus status) {
        this.setStatus(status);
        this.setProcessedAt(Instant.now());
    }
}
