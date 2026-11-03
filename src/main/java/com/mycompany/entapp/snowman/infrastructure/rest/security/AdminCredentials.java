/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Administrator credentials, supplied at deploy time via {@code snowman.admin.username} /
 * {@code snowman.admin.password} (system properties, or the SNOWMAN_ADMIN_USERNAME /
 * SNOWMAN_ADMIN_PASSWORD environment variables). When either is blank, administrator
 * access is disabled and every {@link AdminOnly} endpoint is refused.
 */
@Component
public class AdminCredentials {

    private final byte[] usernameDigest;
    private final byte[] passwordDigest;

    public AdminCredentials(@Value("${snowman.admin.username:}") String username,
                            @Value("${snowman.admin.password:}") String password) {
        if (isBlank(username) || isBlank(password)) {
            this.usernameDigest = null;
            this.passwordDigest = null;
        } else {
            this.usernameDigest = sha256(username);
            this.passwordDigest = sha256(password);
        }
    }

    public boolean isConfigured() {
        return usernameDigest != null;
    }

    public boolean matches(String username, String password) {
        if (!isConfigured() || username == null || password == null) {
            return false;
        }
        boolean usernameMatches = MessageDigest.isEqual(usernameDigest, sha256(username));
        boolean passwordMatches = MessageDigest.isEqual(passwordDigest, sha256(password));
        return usernameMatches & passwordMatches;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
