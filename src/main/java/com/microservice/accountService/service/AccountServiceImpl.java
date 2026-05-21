package com.microservice.accountService.service;

import com.microservice.accountService.domain.AccountDocument;

import com.microservice.accountService.dto.AccountResponseDTO;
import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.AccountServiceException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.mapper.AccountMapper;
import com.microservice.accountService.repository.AccountRepository;
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
    public void reserveFunds(String sourceUserId, BigDecimal amount) throws InsufficientFundsException, AccountNotFoundException {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountServiceException("La cantidad a transferir debe ser mayor que 0");
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
    }

    @Transactional
    @Override
    public boolean ReleaseReserveFunds(String accountNumber, BigDecimal amount) throws InsufficientFundsException, AccountNotFoundException {

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountServiceException("La cantidad a transferir debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new AccountNotFoundException("Cuenta no encontrada - No se han podido reservar los fondos en la cuenta: ", accountNumber)
        );

        if (account.getReservedAmount().compareTo(amount) < 0) {
            throw new InsufficientFundsException("No se puede completar la liberacion de la reserva, por valor superior al reservado.");
        }

        account.setReservedAmount(account.getReservedAmount().subtract(amount));
        account.setAvailableAmount(account.getAvailableAmount().add(amount));

        accountRepository.save(account);

        return true;
    }

    /**
     * Lógica de Descuento/Finalización del dinero transaccional.
     * Este es el paso final y la confirmación de la transacción.
     */
    @Transactional
    @Override
    public void debitAccount(String userId, BigDecimal amount) throws AccountNotFoundException {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountServiceException("La cantidad a transferir debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido debitar los fondos en: ", userId)
        );

        if (account.getReservedAmount().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("No se puede completar la transferencia");
        }

        account.setAccountBalance(account.getAccountBalance().subtract(amount));
        account.setReservedAmount(account.getReservedAmount().subtract(amount));
        accountRepository.save(account);
    }

    /**
     * Lógica de Ingreso del dinero transaccional.
     */
    @Transactional
    @Override
    public AccountResponseDTO creditAccount(String userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountServiceException("La cantidad a ingresar debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByUserId(userId).orElseThrow(
                () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido ingresar los fondos en: ", userId)
        );

        account.setAccountBalance(account.getAccountBalance().add(amount));
        account.setAvailableAmount(account.getAvailableAmount().add(amount));

        accountRepository.save(account);

        return accountMapper.toDTO(account);
    }

    /**
     * Lógica de devolucion en caso de fallo.
     */
    @Transactional
    @Override
    public AccountResponseDTO refundAccount(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountServiceException("La cantidad a devolver debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido devolver los fondos en: ", accountNumber)
        );

        account.setReservedAmount(account.getReservedAmount().subtract(amount));
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
            throw new AccountServiceException("Account already closed"
            );
        }

        account.setStatus("CLOSED");
        accountRepository.save(account);

        return accountMapper.toDTO(account);
    }


}
