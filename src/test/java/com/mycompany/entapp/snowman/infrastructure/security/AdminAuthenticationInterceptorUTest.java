/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminAuthenticationInterceptorUTest {

    private static final String ADMIN_HASH = PasswordHasher.hash("admin-secret");

    private final AdminAuthenticationInterceptor interceptor =
        new AdminAuthenticationInterceptor("admin", ADMIN_HASH);

    @Test
    public void requestWithoutCredentialsIsRejected() {
        assertRejected(interceptor, null);
    }

    @Test
    public void requestWithAdminCredentialsIsAllowed() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        assertTrue(interceptor.preHandle(request(basic("admin", "admin-secret")), response, null));
        Mockito.verifyZeroInteractions(response);
    }

    @Test
    public void wrongPasswordIsRejected() {
        assertRejected(interceptor, basic("admin", "wrong"));
    }

    @Test
    public void wrongUsernameIsRejected() {
        assertRejected(interceptor, basic("username", "admin-secret"));
    }

    @Test
    public void malformedHeadersAreRejected() {
        assertRejected(interceptor, "Bearer abc");
        assertRejected(interceptor, "Basic !!!not-base64");
        assertRejected(interceptor, "Basic " + Base64.getEncoder().encodeToString(
            "admin".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void unconfiguredAdministratorRejectsEverything() {
        assertRejected(new AdminAuthenticationInterceptor("", ""), basic("", ""));
        assertRejected(new AdminAuthenticationInterceptor(null, null), basic("admin", "admin-secret"));
    }

    @Test
    public void plainTextConfiguredPasswordIsNotAccepted() {
        assertRejected(new AdminAuthenticationInterceptor("admin", "admin-secret"), basic("admin", "admin-secret"));
    }

    private static void assertRejected(AdminAuthenticationInterceptor interceptor, String authorization) {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        assertFalse(interceptor.preHandle(request(authorization), response, null));
        Mockito.verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Mockito.verify(response).setHeader(Mockito.eq("WWW-Authenticate"), Mockito.startsWith("Basic "));
    }

    private static HttpServletRequest request(String authorization) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn(authorization);
        return request;
    }

    private static String basic(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
