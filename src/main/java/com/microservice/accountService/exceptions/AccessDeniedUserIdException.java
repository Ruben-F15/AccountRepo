package com.microservice.accountService.exceptions;

public class AccessDeniedUserIdException extends RuntimeException {
    public AccessDeniedUserIdException(String validUserId, String invalidUserId) {
        super("INTENTO DE RESERVA DE FONDOS INICIADO POR USUARIO CON ID: " + invalidUserId
                + "/n EL USUARIO REGISTRADO EN LA APLICACION TIENE ID: " + validUserId);
    }
}
