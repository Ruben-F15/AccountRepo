package com.microservice.accountService.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;


@Getter
@Setter
@Builder
@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
public class AccountDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // MySQL gestionará el ID primario
    private Long id;

    // Foreign Key: Usamos String para mantener la consistencia de tipos de datos
    // independientemente de si el usuario viene de Mongo/Otro origen.
    // Esto es un requisito de interoperabilidad de la arquitectura.
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    // Info de la Cuenta
    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "accountBalance")
    private BigDecimal accountBalance; // Usamos BigDecimal!

    @Column(name = "lastTransactionDate")
    private Instant lastTransactionDate;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status", nullable = false)
    private String status; // Ejemplo: ACTIVE, BLOCKED, CLOSED

    // --- Gestión de Fondos CRÍTICA ---
    // El dinero disponible (lo que sí puede gastar).
    @Column(name = "availableAmount")
    private BigDecimal availableAmount;

    // El dinero que está reservado o bloqueado por transacciones pendientes.
    @Column(name = "reservedAmount")
    private BigDecimal reservedAmount;

    // Metadata
    @Column(name = "openedAt", nullable = false)
    private Instant openedAt;
}
