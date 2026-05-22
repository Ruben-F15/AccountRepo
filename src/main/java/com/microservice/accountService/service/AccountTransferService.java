package com.microservice.accountService.service;

import com.microservice.accountService.exceptions.AccountNotFoundException;
import com.microservice.accountService.exceptions.InsufficientFundsException;
import com.microservice.accountService.kafka.event.TransferCreditRequestedEvent;
import com.microservice.accountService.kafka.event.TransferDebitRequestedEvent;
import com.microservice.accountService.kafka.event.TransferRequestedEvent;
import org.springframework.stereotype.Service;

@Service
public interface AccountTransferService {

    void handleTransferRequest(TransferRequestedEvent event) throws InsufficientFundsException, AccountNotFoundException;

    void handleTransferDebitRequest(TransferDebitRequestedEvent debitRequestedEvent) throws InsufficientFundsException, AccountNotFoundException;

    void handleTransferCreditRequest(TransferCreditRequestedEvent creditRequestedEvent);
}
