package com.microservice.accountService.mapper;

import com.microservice.accountService.domain.AccountDocument;
import com.microservice.accountService.dto.AccountResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponseDTO toDTO(AccountDocument accountDocument) {
        return AccountResponseDTO.builder()
                .id(accountDocument.getId())
                .accountBalance(accountDocument.getAccountBalance())
                .accountNumber(accountDocument.getAccountNumber())
                .userId(accountDocument.getUserId())
                .availableAmount(accountDocument.getAvailableAmount())
                .currency(accountDocument.getCurrency())
                .reservedAmount(accountDocument.getReservedAmount())
                .status(accountDocument.getStatus())
                .build();
    }
}
