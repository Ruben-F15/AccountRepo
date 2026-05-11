package com.microservice.accountService.controller;

import com.microservice.accountService.dto.AccountResponseDTO;
import com.microservice.accountService.dto.CreditAccountRequestDTO;
import com.microservice.accountService.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
//    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponseDTO> getAccountByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(accountService.getAccountByUserID(userId));
    }
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponseDTO> postCreditAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody CreditAccountRequestDTO creditAccountRequestDTO) {
        return  ResponseEntity.ok(accountService.creditAccount(accountNumber, creditAccountRequestDTO.amount()));
    }


}
