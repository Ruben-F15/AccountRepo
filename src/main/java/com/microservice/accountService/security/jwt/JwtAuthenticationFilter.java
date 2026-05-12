package com.microservice.accountService.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private final JwtService jwtService;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("::::::::::::::::::: do filter internal");
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorizationHeader.substring(7);

        log.info(" :::::::::::::::::::::::::::::::::::::::::::::::: DO FILTER");
        System.out.println(" :::::::::::::::::::::::::::::::::::::::::::::::: DO FILTER");
        if (jwtService.isTokenValid(jwt)) {
            log.info(":::::::::::::::::::::::::::::: TOKEN VALIDO");
            Claims claims = jwtService.extractAllClaims(jwt);
            log.info("::::::::::::::::::::::::::: claims: " + claims.get("role").toString());

            String userId = claims.getSubject();

            Object rolesClaim = claims.get("role");

            List<String> roles;

            if (rolesClaim instanceof List<?> rawList) {
                roles = rawList.stream()
                        .map(Object::toString)
                        .toList();
            } else {
                roles = List.of();
            }
            Collection<? extends GrantedAuthority> authorities =
                    roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();
//            List<String> roles = (List<String>) claims.get("role");
//            List<GrantedAuthority> authorities =
//                    roles.stream()
//                            .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
//                            .toList();

            System.out.println("Authorities: " + authorities);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
