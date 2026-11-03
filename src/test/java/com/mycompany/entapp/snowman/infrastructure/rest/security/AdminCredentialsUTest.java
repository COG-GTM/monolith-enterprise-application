/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminCredentialsUTest {

    @Test
    public void matchesOnlyTheConfiguredUsernameAndPassword() {
        AdminCredentials credentials = new AdminCredentials("admin", "s3cret");

        assertTrue(credentials.isConfigured());
        assertTrue(credentials.matches("admin", "s3cret"));
        assertFalse(credentials.matches("admin", "s3cret "));
        assertFalse(credentials.matches("Admin", "s3cret"));
        assertFalse(credentials.matches("admin", null));
        assertFalse(credentials.matches(null, "s3cret"));
    }

    @Test
    public void blankConfigurationDisablesAdministratorAccess() {
        assertFalse(new AdminCredentials("", "").isConfigured());
        assertFalse(new AdminCredentials("admin", " ").isConfigured());
        assertFalse(new AdminCredentials(null, "s3cret").isConfigured());
        assertFalse(new AdminCredentials("", "").matches("", ""));
    }
}
