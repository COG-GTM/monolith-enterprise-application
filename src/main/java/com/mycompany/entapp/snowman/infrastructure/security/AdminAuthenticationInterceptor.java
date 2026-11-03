/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Requires HTTP Basic credentials of the configured administrator. Fails closed (401) when no
 * administrator is configured.
 */
public class AdminAuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAuthenticationInterceptor.class);

    private static final String BASIC_PREFIX = "Basic ";
    private static final String CHALLENGE = "Basic realm=\"snowman\", charset=\"UTF-8\"";

    private final String adminUsername;
    private final String adminPasswordHash;

    public AdminAuthenticationInterceptor(String adminUsername, String adminPasswordHash) {
        this.adminUsername = adminUsername == null ? "" : adminUsername.trim();
        this.adminPasswordHash = adminPasswordHash == null ? "" : adminPasswordHash.trim();
        if (!isConfigured()) {
            LOGGER.warn("No administrator credentials configured (snowman.admin.username / "
                + "snowman.admin.password.hash); protected endpoints will reject every request");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (isConfigured() && isAdmin(request.getHeader("Authorization"))) {
            return true;
        }
        response.setHeader("WWW-Authenticate", CHALLENGE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
    }

    private boolean isConfigured() {
        return !adminUsername.isEmpty() && PasswordHasher.isHash(adminPasswordHash);
    }

    private boolean isAdmin(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            return false;
        }
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authorization.substring(BASIC_PREFIX.length()).trim()),
                StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return false;
        }
        int separator = decoded.indexOf(':');
        if (separator < 0) {
            return false;
        }
        String username = decoded.substring(0, separator);
        String password = decoded.substring(separator + 1);
        boolean usernameMatches = MessageDigest.isEqual(
            username.getBytes(StandardCharsets.UTF_8), adminUsername.getBytes(StandardCharsets.UTF_8));
        boolean passwordMatches = PasswordHasher.matches(password, adminPasswordHash);
        return usernameMatches && passwordMatches;
    }
}
