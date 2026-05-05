package com.microservice.accountService.repository;

import com.microservice.accountService.domain.AccountDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountDocument, Long> {

    Optional<AccountDocument> findByUserId(String userId);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByUserId(String userId);
}
