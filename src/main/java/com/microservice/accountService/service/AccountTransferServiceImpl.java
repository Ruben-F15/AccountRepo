package com.microservice.accountService.service;

import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.exceptions.InvalidAmountException;
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
        try {
            log.info("::::::: Reserving funds...");
            accountService.reserveFunds(event.sourceUserId(), event.amount(), event.transactionId());
            log.info("::::::: Reserved funds...");
        } catch (InvalidAmountException ex) {
            log.error("Invalid amount to reserve transaction={}", event.transactionId(), ex);
            accountEventProducer.sendFundsReservationFailedEvent(new FundsReservationFailedEvent(
                    event.transactionId(),
                    EventErrors.INVALID_AMOUNT.getError(),
                    event.sourceUserId()
            ));
            return;
        } catch (InsufficientFundsException ex) {
            log.error("Failed reserving funds transaction={}", event.transactionId(), ex);
            accountEventProducer.sendFundsReservationFailedEvent(new FundsReservationFailedEvent(
                    event.transactionId(),
                    EventErrors.INSUFFICIENT_FUNDS.getError(),
                    event.sourceUserId()
            ));
            return;
        } catch (AccountNotFoundException ex) {
            log.error("Account not found for user={}", event.sourceUserId(), ex);
            accountEventProducer.sendFundsReservationFailedEvent(new FundsReservationFailedEvent(
                    event.transactionId(),
                    EventErrors.ACCOUNT_NOT_FOUND.getError(),
                    event.sourceUserId()
            ));
            return;
        }

        log.info("::::::: Sending fundsReservedEvent...");
        accountEventProducer.sendFundsReservedEvent(new FundsReservedEvent(
                event.sourceUserId(),
                event.transactionId()
        ));
        log.info("::::::: Sent fundsReservedEvent...");
    }

    @Override
    public void handleTransferDebitRequest(TransferDebitRequestedEvent event) {
        try {
            accountService.debitAccount(event.sourceUserId(), event.amount(), event.transactionId());
        } catch (InvalidAmountException ex) {
            log.error("Failed debiting funds transaction={}", event.transactionId(), ex);
            accountService.releaseReserveFunds(event.sourceUserId(), event.amount(), event.transactionId());
            accountEventProducer.sendFundsDebitFailedEvent(new FundsDebitFailedEvent(
                    event.transactionId(),
                    EventErrors.INSUFFICIENT_FUNDS.getError(),
                    event.sourceUserId()
            ));
            return;
        } catch (AccountNotFoundException ex) {
            log.error("Account not found for user={}", event.sourceUserId(), ex);
            accountService.releaseReserveFunds(event.sourceUserId(), event.amount(), event.transactionId());
            accountEventProducer.sendFundsDebitFailedEvent(new FundsDebitFailedEvent(
                    event.transactionId(),
                    EventErrors.ACCOUNT_NOT_FOUND.getError(),
                    event.sourceUserId()
            ));
            return;
        }

        accountEventProducer.sendFundsDebitedEvent(new FundsDebitedEvent(
                event.sourceUserId(),
                event.transactionId()
        ));
    }

    @Override
    public void handleTransferCreditRequest(TransferCreditRequestedEvent event) {
       try {
           accountService.creditAccount(event.destinationUserId(), event.amount(), event.transactionId());
       } catch (InvalidAmountException ex) {
           log.error("Failed crediting funds transaction={}", event.transactionId(), ex);
           accountService.refundAccount(event.sourceUserId(), event.amount(), event.transactionId());
           accountEventProducer.sendFundsCreditFailedEvent(new FundsCreditFailedEvent(
                   event.transactionId(),
                   EventErrors.INSUFFICIENT_FUNDS.getError(),
                   event.sourceUserId()
           ));
           return;
       } catch (AccountNotFoundException ex) {
           log.error("Account not found for user={}", event.sourceUserId(), ex);
           accountService.refundAccount(event.sourceUserId(), event.amount(), event.transactionId());
           accountEventProducer.sendFundsCreditFailedEvent(new FundsCreditFailedEvent(
                   event.transactionId(),
                   EventErrors.ACCOUNT_NOT_FOUND.getError(),
                   event.sourceUserId()
           ));
           return;
       }

        accountEventProducer.sendFundsCreditedEvent(new FundsCreditedEvent(
                event.sourceUserId(),
                event.transactionId()));
    }
}
