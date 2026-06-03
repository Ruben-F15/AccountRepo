package com.microservice.accountService.service;


import com.microservice.accountService.domain.AccountDocument;
import com.microservice.accountService.domain.TransactionOperation;
import com.microservice.accountService.domain.enums.TransactionOperationStatus;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.repository.AccountRepository;
import com.microservice.accountService.repository.TransactionOperationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionOperationRepository transactionOperationRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldReserveFundsSuccessfully() {

        String transactionId = "tx-1";

        AccountDocument account = AccountDocument.builder()
                .userId("user-1")
                .availableAmount(new BigDecimal("100.00"))
                .reservedAmount(BigDecimal.ZERO)
                .build();

        TransactionOperation operation = new TransactionOperation(
                        transactionId,
                        TransactionOperationStatus.CREATED,
                        Instant.now()
                );

        when(transactionOperationRepository.findById(transactionId)).thenReturn(Optional.of(operation));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        accountService.reserveFunds("user-1", new BigDecimal("50.00"), transactionId);

        assertEquals(new BigDecimal("50.00"), account.getAvailableAmount());
        assertEquals(new BigDecimal("50.00"), account.getReservedAmount());
        assertEquals(TransactionOperationStatus.RESERVED, operation.getStatus());

        verify(accountRepository).save(account);

        verify(transactionOperationRepository).save(operation);
    }

    @Test
    void shouldThrowWhenInsufficientFunds() {
        String transactionId = "tx-1";

        AccountDocument account = AccountDocument.builder()
                .userId("user-1")
                .availableAmount(new BigDecimal("10.00"))
                .reservedAmount(BigDecimal.ZERO)
                .build();

        TransactionOperation operation = new TransactionOperation(
                transactionId,
                TransactionOperationStatus.CREATED,
                Instant.now()
        );

        when(transactionOperationRepository.findById(transactionId)).thenReturn(Optional.of(operation));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        assertThrows(
                InsufficientFundsException.class,
                () -> accountService.reserveFunds("user-1", new BigDecimal("500.00"), transactionId));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRefundAccountSuccessfully() {
        String transactionId = "tx-1";

        AccountDocument account = AccountDocument.builder()
                .userId("user-1")
                .availableAmount(new BigDecimal("50.00"))
                .accountBalance(new BigDecimal("50.00"))
                .build();

        TransactionOperation operation = new TransactionOperation(
                transactionId,
                TransactionOperationStatus.DEBITED,
                Instant.now()
        );

        when(transactionOperationRepository.findById(transactionId)).thenReturn(Optional.of(operation));

        when(accountRepository.findByUserId("user-1")).thenReturn(Optional.of(account));

        // ACT
        accountService.refundAccount("user-1", new BigDecimal("25.00"), transactionId);

        // ASSERT
        assertEquals(new BigDecimal("75.00"), account.getAccountBalance());

        assertEquals(new BigDecimal("75.00"), account.getAvailableAmount());

        assertEquals(TransactionOperationStatus.REFUNDED, operation.getStatus());

        // VERIFY
        verify(accountRepository).save(account);

        verify(transactionOperationRepository).save(operation);
    }

}
