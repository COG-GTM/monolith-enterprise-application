/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class PasswordHasherUTest {

    @Test
    public void hashIsSaltedAndVerifiable() {
        String first = PasswordHasher.hash("correct horse");
        String second = PasswordHasher.hash("correct horse");

        assertTrue(PasswordHasher.isHash(first));
        assertFalse(first.contains("correct horse"));
        assertNotEquals(first, second);
        assertTrue(PasswordHasher.matches("correct horse", first));
        assertTrue(PasswordHasher.matches("correct horse", second));
    }

    @Test
    public void wrongPasswordDoesNotMatch() {
        String hash = PasswordHasher.hash("correct horse");

        assertFalse(PasswordHasher.matches("battery staple", hash));
        assertFalse(PasswordHasher.matches(null, hash));
    }

    @Test
    public void plainTextOrMalformedStoredValuesNeverMatch() {
        assertFalse(PasswordHasher.matches("password", "password"));
        assertFalse(PasswordHasher.matches("password", null));
        assertFalse(PasswordHasher.matches("password", "pbkdf2_sha256$abc$def"));
        assertFalse(PasswordHasher.matches("password", "pbkdf2_sha256$x$c2FsdA$aGFzaA"));
        assertFalse(PasswordHasher.matches("password", "pbkdf2_sha256$0$c2FsdA$aGFzaA"));
        assertFalse(PasswordHasher.matches("password", "pbkdf2_sha256$1000$!!$aGFzaA"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyPasswordIsRejected() {
        PasswordHasher.hash("");
    }
}
