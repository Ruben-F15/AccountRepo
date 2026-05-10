package com.microservice.accountService.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record CreditAccountRequestDTO(
//        @NotNull(message = "account Number cannot be Null or empty.")
//        @Size(min = 22, message = "Account Number must have 22 characters")
//        String accountNumber,

        @NotNull(message = "Amount cannot be null or empty.")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0.01")
        BigDecimal amount
) {
}
