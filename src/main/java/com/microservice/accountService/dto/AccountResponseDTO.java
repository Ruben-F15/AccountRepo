package com.microservice.accountService.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record AccountResponseDTO(
        Long id,
        String userId,
        String accountNumber,
        BigDecimal accountBalance,
        String currency,
        String status,
        BigDecimal availableAmount,
        BigDecimal reservedAmount
) {
}
