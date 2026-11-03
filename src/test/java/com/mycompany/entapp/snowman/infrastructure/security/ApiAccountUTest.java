/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

import org.junit.Test;

import static org.junit.Assert.*;

public class ApiAccountUTest {

    @Test
    public void givenCredentials_whenMatches_thenOnlyExactPairMatches() {
        ApiAccount account = new ApiAccount("user", "secret", ApiRole.VIEWER);

        assertTrue(account.isEnabled());
        assertTrue(account.matches("user", "secret"));
        assertFalse(account.matches("user", "Secret"));
        assertFalse(account.matches("other", "secret"));
        assertFalse(account.matches(null, null));
    }

    @Test
    public void givenBlankCredentials_whenMatches_thenDisabledAndNeverMatches() {
        ApiAccount account = new ApiAccount(" ", "", ApiRole.ADMIN);

        assertFalse(account.isEnabled());
        assertFalse(account.matches(" ", ""));
        assertFalse(account.matches("", ""));
    }

    @Test
    public void givenAdminRole_whenGrants_thenImpliesViewer() {
        assertTrue(ApiRole.ADMIN.grants(ApiRole.VIEWER));
        assertTrue(ApiRole.ADMIN.grants(ApiRole.ADMIN));
        assertTrue(ApiRole.VIEWER.grants(ApiRole.VIEWER));
        assertFalse(ApiRole.VIEWER.grants(ApiRole.ADMIN));
    }
}
