package com.microservice.accountService.infrastructure.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    // CLASE DEDICADA AL CORRELATION ID GENERAL, PARA TRACING Y DEBUG.

    public static  final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Buscamos si el cliente ya envió un Correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);

            // Si no existe, generamos uno nuevo
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Lo guardamos en MDC para que aparezca en logs
            MDC.put("correlationId", correlationId);

            // Lo devolvemos también en la respuesta
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continuamos la cadena de filtros
            filterChain.doFilter(request, response);
        } finally {
            // limpiamos MDC para evitar contaminación entre requests
            MDC.clear();
        }
    }
}
