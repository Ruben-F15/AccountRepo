package com.microservice.accountService.service;

import com.microservice.accountService.domain.AccountDocument;

import com.microservice.accountService.domain.TransactionOperation;
import com.microservice.accountService.domain.enums.TransactionOperationStatus;
import com.microservice.accountService.dto.AccountResponseDTO;
import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.InvalidAmountException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.mapper.AccountMapper;
import com.microservice.accountService.repository.AccountRepository;
import com.microservice.accountService.repository.TransactionOperationRepository;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@AllArgsConstructor
@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final TransactionOperationRepository transactionOperationRepository;

    @Override
    public void createAccount(String userId) {
        log.info("Creating account..... ");

        if (accountRepository.existsByUserId(userId)) {
            log.info("The user already has an account created for its id:  {}", userId);
            return; // idempotencia, asegura que no existe y no se duplica.
        }

        accountRepository.save(AccountDocument.builder()
                .accountNumber(generateUniqueAccountNumber())
                .accountBalance(BigDecimal.ZERO)
                .availableAmount(BigDecimal.ZERO)
                .currency("EUR")
                .openedAt(Instant.now())
                .reservedAmount(BigDecimal.ZERO)
                .userId(userId)
                .status("ACTIVE")
                .build());
        log.info("Created account for userId: {}", userId);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "ES" + String.format("%020d",
                    ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L)
            );
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    /**
     * Ejecuta la Lógica de Negocio atómica contra la base de datos.
     *
     * @Transactional es clave aquí.
     */
    @Transactional
    @Override
    public void reserveFunds(String sourceUserId, BigDecimal amount, String transactionId) {

        TransactionOperation transactionOperation = transactionOperationRepository.findById(transactionId).orElseGet(
                () -> transactionOperationRepository.save(new TransactionOperation(
                        transactionId,
                        TransactionOperationStatus.CREATED,
                        Instant.now())

                )
        );

        if (transactionOperation.getStatus().equals(TransactionOperationStatus.CREATED)) {
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidAmountException("La cantidad a transferir debe ser mayor que 0");
            }

            AccountDocument account = accountRepository.findByUserId(sourceUserId).orElseThrow(
                    () -> new AccountNotFoundException("Account not found for user with id: ", sourceUserId)
            );

            if (account.getAvailableAmount().compareTo(amount) < 0) {
                throw new InsufficientFundsException("No se puede completar la transferencia, no hay fondos suficientes");
            }

            account.setAvailableAmount(account.getAvailableAmount().subtract(amount));
            account.setReservedAmount(account.getReservedAmount().add(amount));

            accountRepository.save(account);
            transactionOperation.updateStatus(TransactionOperationStatus.RESERVED);
            transactionOperationRepository.save(transactionOperation);
        } else if (transactionOperation.getStatus().equals(TransactionOperationStatus.RESERVED)) {
            log.info("::::::: TRANSACTION ALREADY RESERVED :::::::::");
        } else {
            log.info("::::::: TRANSACTION STATUS NON EXPECTED = {}", transactionOperation.getStatus());
            throw new InvalidAmountException("TRANSACTION STATUS NON EXPECTED");
        }
    }

    @Transactional
    @Override
    public void releaseReserveFunds(String sourceUserId, BigDecimal amount, String transactionId) {

        TransactionOperation transactionOperation = transactionOperationRepository.findById(transactionId).orElseThrow();

        if (transactionOperation.getStatus().equals(TransactionOperationStatus.RESERVED)) {

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidAmountException("La cantidad a transferir debe ser mayor que 0");
            }

            AccountDocument account = accountRepository.findByUserId(sourceUserId).orElseThrow(
                    () -> new AccountNotFoundException("Compensation not done. Account not found for user with id: ", sourceUserId)
            );

            if (account.getReservedAmount().compareTo(amount) < 0) {
                log.info("No se puede completar la liberacion de la reserva, por valor superior al reservado.{}", sourceUserId);
                throw new InvalidAmountException("No se puede completar la liberacion de la reserva, por valor superior al reservado");
            }

            account.setReservedAmount(account.getReservedAmount().subtract(amount));
            account.setAvailableAmount(account.getAvailableAmount().add(amount));

            accountRepository.save(account);
            transactionOperation.updateStatus(TransactionOperationStatus.RELEASED);
            transactionOperationRepository.save(transactionOperation);
            log.info("Released account for userId: {}", account.getUserId());
        } else {
            log.info("Release reserve funds not done because transactionOperation= {}", transactionOperation.getStatus());
        }
    }

    /**
     * Lógica de Descuento/Finalización del dinero transaccional.
     * Este es el paso final y la confirmación de la transacción.
     */
    @Transactional
    @Override
    public void debitAccount(String userId, BigDecimal amount, String transactionId) {

        TransactionOperation transactionOperation = transactionOperationRepository.findById(transactionId).orElseThrow();

        if (transactionOperation.getStatus().equals(TransactionOperationStatus.RESERVED)) {

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidAmountException("La cantidad a transferir debe ser mayor que 0");
            }

            AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                    () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido debitar los fondos en: ", userId)
            );

            if (account.getReservedAmount().compareTo(amount) < 0) {
                throw new InvalidAmountException("No se puede completar la transferencia no hay fondos reservados suficientes");
            }

            account.setAccountBalance(account.getAccountBalance().subtract(amount));
            account.setReservedAmount(account.getReservedAmount().subtract(amount));
            accountRepository.save(account);
            transactionOperation.updateStatus(TransactionOperationStatus.DEBITED);
            transactionOperationRepository.save(transactionOperation);
        } else {
            if (transactionOperation.getStatus().equals(TransactionOperationStatus.DEBITED)) {
                log.info(":::::: FUNDS ALREADY DEBITED FOR TRANSACTIONID= {}", transactionId);
            }
            log.info("debit funds not done because transactionOperation= {}", transactionOperation.getStatus());
        }
    }

    /**
     * Lógica de Ingreso del dinero transaccional.
     */
    @Transactional
    @Override
    public void creditAccount(String userId, BigDecimal amount, String transactionId) {

        TransactionOperation transactionOperation = transactionOperationRepository.findById(transactionId).orElseThrow();

        if (transactionOperation.getStatus().equals(TransactionOperationStatus.DEBITED)) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAmountException("La cantidad a ingresar debe ser mayor que 0");
            }

            AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                    () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido ingresar los fondos en: ", userId)
            );

            account.setAccountBalance(account.getAccountBalance().add(amount));
            account.setAvailableAmount(account.getAvailableAmount().add(amount));

            accountRepository.save(account);
            transactionOperation.updateStatus(TransactionOperationStatus.CREDITED);
            transactionOperationRepository.save(transactionOperation);
        } else {
            if (transactionOperation.getStatus().equals(TransactionOperationStatus.CREDITED)) {
                log.info(":::::: FUNDS ALREADY CREDITED FOR TRANSACTIONID= {}", transactionId);
            }
            log.info("credit funds not done because transactionOperation= {}", transactionOperation.getStatus());
        }
    }

    /**
     * Lógica de devolucion en caso de fallo.
     */
    @Transactional
    @Override
    public void refundAccount(String userId, BigDecimal amount, String transactionId) {
        TransactionOperation transactionOperation = transactionOperationRepository.findById(transactionId).orElseThrow();

        if (transactionOperation.getStatus().equals(TransactionOperationStatus.DEBITED)
                || transactionOperation.getStatus().equals(TransactionOperationStatus.REVERSED)) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAmountException("La cantidad a ingresar debe ser mayor que 0");
            }

            AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                    () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido ingresar los fondos en: ", userId)
            );

            account.setAccountBalance(account.getAccountBalance().add(amount));
            account.setAvailableAmount(account.getAvailableAmount().add(amount));

            accountRepository.save(account);
            transactionOperation.updateStatus(TransactionOperationStatus.REFUNDED);
            transactionOperationRepository.save(transactionOperation);
        } else if (transactionOperation.getStatus().equals(TransactionOperationStatus.REFUNDED)) {
            log.info(":::::: FUNDS ALREADY REFUNDED FOR TRANSACTIONID= {}", transactionId);
        } else {
            log.info("refund not done because transactionOperation= {}", transactionOperation.getStatus());
        }
    }

    @Transactional
    @Override
    public void reverseCredit(String userId, BigDecimal amount, String transactionId) {
        TransactionOperation transactionOperation = transactionOperationRepository.findById(transactionId).orElseThrow();

        if (transactionOperation.getStatus().equals(TransactionOperationStatus.CREDITED)) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidAmountException("La cantidad a revertir debe ser mayor que 0");
            }

            AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                    () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido revertir los fondos en: ", userId)
            );

            account.setAccountBalance(account.getAccountBalance().subtract(amount));
            account.setAvailableAmount(account.getAvailableAmount().subtract(amount));

            accountRepository.save(account);
            transactionOperation.updateStatus(TransactionOperationStatus.REVERSED);
            transactionOperationRepository.save(transactionOperation);
        } else if (transactionOperation.getStatus().equals(TransactionOperationStatus.REVERSED)) {
            log.info(":::::: FUNDS ALREADY REVERSED FOR TRANSACTIONID= {}", transactionId);
        } else {
            log.info("reverse credit not done because transactionOperation= {}", transactionOperation.getStatus());

        }
    }

    @Transactional
    @Override
    public AccountResponseDTO depositAccount(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("La cantidad a ingresar debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido ingresar los fondos en: ", userId)
        );

        account.setAccountBalance(account.getAccountBalance().add(amount));
        account.setAvailableAmount(account.getAvailableAmount().add(amount));

        accountRepository.save(account);

        return accountMapper.toDTO(account);
    }


    @Override
    public AccountResponseDTO getAccountDTOByUserID(String userId) {
        AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                () -> new AccountNotFoundException("Account not found for user with id: ", userId)
        );

        return accountMapper.toDTO(account);
    }

    @Override
    public AccountDocument getAccountByUserId(String userId) {
        return accountRepository.findByUserId(userId).orElseThrow(
                () -> new AccountNotFoundException("Account not found for user with id: ", userId)
        );
    }

    @Transactional
    @Override
    public AccountResponseDTO closeAccountById(Long accountId) {
        AccountDocument account = accountRepository.findById(accountId).orElseThrow(
                () -> new AccountNotFoundException("Account not found for account id: ", accountId.toString())
        );

        if (account.getStatus().equals("CLOSED")) {
            throw new InvalidAmountException("Account already closed"
            );
        }

        account.setStatus("CLOSED");
        accountRepository.save(account);

        return accountMapper.toDTO(account);
    }


}
