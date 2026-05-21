package com.microservice.accountService.service;

import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.kafka.event.*;
import com.microservice.accountService.kafka.producer.AccountEventProducer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AccountTransferServiceImpl implements AccountTransferService {

    private final AccountService accountService;
    private final AccountEventProducer accountEventProducer;


    @Override
    public void handleTransferRequest(TransferRequestedEvent event) {
        FundsReservationFailedEvent fundsReservationFailedEvent = null;
        try {
                    log.info("::::::: Reserving funds...");
            accountService.reserveFunds(event.sourceUserId(), event.amount());
                    log.info("::::::: Reserved funds...");
            FundsReservedEvent fundsReservedEvent = new FundsReservedEvent(
                    event.sourceUserId(),
                    event.transactionId()
            );
                    log.info("::::::: Sending fundsReservedEvent...");
            accountEventProducer.sendFundsReservedEvent(fundsReservedEvent);
                    log.info("::::::: Sent fundsReservedEvent...");
        } catch (InsufficientFundsException ex) {
                    log.error("Failed reserving funds transaction={}", event.transactionId(), ex);
            fundsReservationFailedEvent = new FundsReservationFailedEvent(
                    event.transactionId(),
                    EventErrors.INSUFFICENT_FUNDS.getError(),
                    event.sourceUserId()
            );

        } catch (AccountNotFoundException ex) {
                    log.error("Account not found for user={}", event.sourceUserId(), ex);
            fundsReservationFailedEvent = new FundsReservationFailedEvent(
                    event.transactionId(),
                    EventErrors.ACCOUNT_NOT_FOUND.getError(),
                    event.sourceUserId()
            );
        } finally {
            accountEventProducer.sendFundsReservationFailedEvent(fundsReservationFailedEvent);
        }
    }

    @Override
    public void handleTransferDebitRequest(TransferDebitRequestedEvent event) {

        accountService.debitAccount(event.sourceUserId(), event.amount());

        FundsDebitedEvent fundsDebitedEvent = new FundsDebitedEvent(
                event.sourceUserId(),
                event.transactionId()
        );
        accountEventProducer.sendFundsDebitedEvent(fundsDebitedEvent);
    }
}
