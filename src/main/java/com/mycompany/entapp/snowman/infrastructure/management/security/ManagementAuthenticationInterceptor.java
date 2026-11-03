/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Requires {@code Authorization: Bearer <management.api.token>} on management endpoints.
 * Fails closed when no token is configured.
 */
public class ManagementAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagementAuthenticationInterceptor.class);

    private static final String BEARER_PREFIX = "Bearer ";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final byte[] expectedTokenDigest;

    public ManagementAuthenticationInterceptor(String managementToken) {
        if (managementToken == null || managementToken.trim().isEmpty()) {
            LOGGER.warn("management.api.token is not configured; management endpoints are disabled");
            this.expectedTokenDigest = null;
        } else {
            this.expectedTokenDigest = sha256(managementToken.trim());
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (expectedTokenDigest == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Management endpoints are disabled");
            return false;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return unauthorized(response);
        }

        String presentedToken = authorization.substring(BEARER_PREFIX.length()).trim();
        if (!MessageDigest.isEqual(expectedTokenDigest, sha256(presentedToken))) {
            LOGGER.warn("Rejected management request with an invalid token");
            return unauthorized(response);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
    }

    private static boolean unauthorized(HttpServletResponse response) throws java.io.IOException {
        response.setHeader("WWW-Authenticate", "Bearer realm=\"management\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
