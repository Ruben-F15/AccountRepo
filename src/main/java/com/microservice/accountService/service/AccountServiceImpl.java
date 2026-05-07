package com.microservice.accountService.service;

import com.microservice.accountService.domain.AccountDocument;

import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.AccountServiceException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
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


    @Override
    public void createAccount(String userId) {
        System.out.println("creando cuenta corriente..... sout");
        log.info("creando cuenta corriente..... ");
        if (accountRepository.existsByUserId(userId)) {
            log.info("Account already exists for userId {}", userId);
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
     * @Transactional es clave aquí.
     */
    @Transactional
    @Override
    public boolean reserveFunds(String accountId, BigDecimal amount) throws InsufficientFundsException, AccountNotFoundException {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountServiceException("La cantidad a transferir debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByUserId(accountId).orElseThrow(
                () -> new AccountNotFoundException("No se han podido reservar los fondos", accountId)
        );

        if (account.getAvailableAmount().compareTo(amount) < 0) {
            throw new InsufficientFundsException("No se puede completar la transferencia");
        }

        account.setAvailableAmount(account.getAvailableAmount().subtract(amount));
        account.setReservedAmount(account.getReservedAmount().add(amount));

        accountRepository.save(account);

        return true;
    }

    /**
     * Lógica de Descuento/Finalización del dinero transaccional.
     * Este es el paso final y la confirmación de la transacción.
     */
    @Transactional
    @Override
    public void debitAccount(String accountId, BigDecimal amount) throws AccountNotFoundException {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountServiceException("La cantidad a transferir debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByUserId(accountId).orElseThrow(
                () -> new AccountNotFoundException("No se han podido reservar los fondos", accountId)
        );

        if (account.getReservedAmount().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("No se puede completar la transferencia");
        }

        account.setReservedAmount(account.getReservedAmount().subtract(amount));
        account.setAccountBalance(account.getAccountBalance().subtract(amount));

        accountRepository.save(account);
    }

    @Transactional
    @Override
    public void creditAccount(String accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new AccountServiceException("La cantidad a ingresar debe ser mayor que 0");
        }

        AccountDocument account = accountRepository.findByUserId(accountId).orElseThrow(
                () -> new AccountNotFoundException("Cuenta no encontrada. No se han podido ingresar los fondos", accountId)
        );

        account.setAccountBalance(account.getAccountBalance().add(amount));
        account.setAvailableAmount(account.getAvailableAmount().add(amount));

        accountRepository.save(account);
    }
}
