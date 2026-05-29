package com.microservice.accountService.repository;

import com.microservice.accountService.domain.TransactionOperation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionOperationRepository extends JpaRepository<TransactionOperation, String> {

    Optional<TransactionOperation> findById(String transactionId);

}
