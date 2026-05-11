package com.microservice.accountService.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(DataAccessException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Error Data Access",
                        ex.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.INTERNAL_SERVER_ERROR.toString()

                )
        );
    }

    @ExceptionHandler(AccessDeniedUserIdException.class)
    public ResponseEntity<ApiError> handleAccessDeniedUserIdException(AccessDeniedUserIdException ex, HttpServletRequest request) {
        return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ApiError(
                        Instant.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Acceso denegado",
                        ex.getMessage(),
                        request.getRequestURI(),
                        HttpStatus.FORBIDDEN.toString()
                )
        );
    }



}
