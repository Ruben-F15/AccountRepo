package com.microservice.accountService.service;

import com.microservice.accountService.domain.AccountDocument;
import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.kafka.event.EventErrors;
import com.microservice.accountService.kafka.event.FundsReservationFailedEvent;
import com.microservice.accountService.kafka.event.FundsReservedEvent;
import com.microservice.accountService.kafka.event.TransferRequestedEvent;
import com.microservice.accountService.kafka.producer.AccountEventProducer;
import com.microservice.accountService.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AccountTransferServiceImpl implements AccountTransferService {

    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final AccountEventProducer accountEventProducer;


    @Override
    public void handleTransferRequest(TransferRequestedEvent event) throws InsufficientFundsException, AccountNotFoundException {
        try {
            AccountDocument accountDocument = accountRepository.findByUserId(event.sourceUserId()).orElseThrow(
                    () -> new AccountNotFoundException("Account not found for user with id: ", event.sourceUserId())
            );

            log.info("::::::: Reserving funds...");
            accountService.reserveFunds(accountDocument.getId(), event.amount());
            log.info("::::::: Reserved funds...");
            FundsReservedEvent fundsReservedEvent = new FundsReservedEvent(
                    event.sourceUserId(),
                    event.transactionId()
            );
            log.info("::::::: Sending fundsReservedEvent...");
            accountEventProducer.sendFundsReservedEvent(fundsReservedEvent);
            log.info("::::::: Sent fundsReservedEvent...");
        } catch (InsufficientFundsException | AccountNotFoundException ex) {
            FundsReservationFailedEvent fundsReservationFailedEvent = null;

            if (ex instanceof InsufficientFundsException) {
                log.error("Failed reserving funds transaction={}", event.transactionId(), ex);
                fundsReservationFailedEvent = new FundsReservationFailedEvent(
                        event.transactionId(),
                        EventErrors.INSUFFICENT_FUNDS.getError(),
                        event.sourceUserId()
                );
            } else if (ex instanceof AccountNotFoundException) {
                log.error("Account not found for user={}", event.sourceUserId(), ex);
                fundsReservationFailedEvent = new FundsReservationFailedEvent(
                        event.transactionId(),
                        EventErrors.ACCOUNT_NOT_FOUND.getError(),
                        event.sourceUserId()
                );
            } else {
                log.error("Undefined eror for haldne transfer request={}", event.transactionId(), ex.getMessage());
                fundsReservationFailedEvent = new FundsReservationFailedEvent(
                        event.transactionId(),
                        EventErrors.UNDEFINED_ERROR.getError(),
                        event.sourceUserId()
                );
            }
            accountEventProducer.sendFundsReservationFailedEvent(fundsReservationFailedEvent);
        }
    }
}
