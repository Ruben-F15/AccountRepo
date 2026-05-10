package com.microservice.accountService.service;

import com.microservice.accountService.dto.AccountResponseDTO;
import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface AccountService {

    void createAccount(String userId);

    /**
     * Intenta reservar el monto para prevenir dobles gastos.
     *      * 1. Reserva fondos (Source Account).
     *      * Este method debe ser ATÓMICO a nivel de BD.
     *      *
     *      * Lo que hace:
     *      * 1. Verifica si availableBalance >= amount.
     *      * 2. Si es cierto: availableBalance -= amount; reservedBalance += amount.
     *    Esta es la primera llamada antes de ejecutar la transferencia.
     * @param accountId El ID de la cuenta emisora.
     * @param amount El monto a reservar.
     * @return true si la reserva fue exitosa.
     * @throws InsufficientFundsException si no hay fondos disponibles.
     * @throws AccountNotFoundException si no se encuentra la cuenta
     */
    boolean reserveFunds(String accountId, BigDecimal amount) throws InsufficientFundsException, AccountNotFoundException;

    /**
     * Finaliza la transferencia y descuenta permanentemente los fondos.
     * Confirma el débito permanentemente (Source Account).
     *      * Lo que hace:
     *      * 1. Transfiere los fondos reservados a la cuenta final.
     *      * 2. Resetear reservedBalance a cero (o a un valor normal) para indicar que la cuenta está limpia.
     *    Esta es la segunda llamada, ejecutada *solo* si la reserva fue exitosa.
     * @param accountId El ID de la cuenta emisora a retirar fondos.
     * @param amount El monto a descontar.
     * @throws AccountNotFoundException si la cuenta no existe.
     */
    void debitAccount(String accountId, BigDecimal amount) throws AccountNotFoundException;

    /**
     * Devuelve los fondos en caso de que el proceso falle en medio de la transacción.
     * Compensación de fondos (Source Account).
     *      * Lo que hace:
     *      * 1. Disminuye el reservedBalance.
     *      * 2. Aumenta el availableBalance.
     *    Se llama SIEMPRE que el proceso falló después de la reserva.
     *    Esta es la llamada de compensación, ejecutada en el proceso de rollback.
     * @param accountNumber numero de la cuenta a ingresar.
     * @param amount El monto a acreditar.
     */
    AccountResponseDTO creditAccount(String accountNumber, BigDecimal amount);

    AccountResponseDTO getAccountByUserID(String userId);

    AccountResponseDTO closeAccountById(Long accountId);
}
