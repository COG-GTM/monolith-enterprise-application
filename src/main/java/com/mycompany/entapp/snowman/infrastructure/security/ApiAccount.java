/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * An API account configured from the environment. An account with a blank username or password is disabled.
 */
public class ApiAccount {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final byte[] usernameDigest;
    private final byte[] passwordDigest;
    private final ApiRole role;
    private final boolean enabled;

    public ApiAccount(String username, String password, ApiRole role) {
        if (role == null) {
            throw new IllegalArgumentException("role must not be null");
        }
        this.enabled = !isBlank(username) && !isBlank(password);
        this.usernameDigest = digest(enabled ? username : "");
        this.passwordDigest = digest(enabled ? password : "");
        this.role = role;
    }

    public ApiRole getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Compares in constant time with respect to the supplied values.
     */
    public boolean matches(String username, String password) {
        boolean usernameMatches = MessageDigest.isEqual(usernameDigest, digest(username));
        boolean passwordMatches = MessageDigest.isEqual(passwordDigest, digest(password));
        return enabled & usernameMatches & passwordMatches;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest((value == null ? "" : value).getBytes(UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
