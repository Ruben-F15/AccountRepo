package com.microservice.accountService.repository;

import com.microservice.accountService.domain.AccountDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountDocument, Long> {

    Optional<AccountDocument> findById(String accountId);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByUserId(String userId);
    Optional<AccountDocument> findByAccountNumber(String accountNumber);
    Optional<AccountDocument> findByUserId(String accountId);
}
