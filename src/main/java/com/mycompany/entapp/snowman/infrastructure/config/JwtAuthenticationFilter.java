/*
 * |-------------------------------------------------
 * | Copyright © 2024 Auth0 Integration. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.GrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Filter that extracts and validates JWT Bearer tokens from the Authorization header.
 * Tokens are validated against the configured Auth0 issuer and audience.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final String issuer;
    private final String audience;

    public JwtAuthenticationFilter(String issuer, String audience) {
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());

            try {
                DecodedJWT decodedJWT = JWT.decode(token);

                // Verify issuer
                if (!issuer.equals(decodedJWT.getIssuer())) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token issuer");
                    return;
                }

                // Verify audience
                if (decodedJWT.getAudience() == null || !decodedJWT.getAudience().contains(audience)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token audience");
                    return;
                }

                // Verify token is not expired
                if (decodedJWT.getExpiresAt() != null && decodedJWT.getExpiresAt().getTime() < System.currentTimeMillis()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
                    return;
                }

                String subject = decodedJWT.getSubject();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, new ArrayList<GrantedAuthority>());
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JWTVerificationException ex) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
