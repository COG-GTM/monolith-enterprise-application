/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.management.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ManagementAuthenticationInterceptorUTest {

    private static final String TOKEN = "s3cr3t-management-token";

    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void testPreHandle_validBearerToken_allowsRequest() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer " + TOKEN);

        assertTrue(new ManagementAuthenticationInterceptor(TOKEN).preHandle(request, response, null));
        Mockito.verify(response, Mockito.never()).sendError(Mockito.anyInt());
        Mockito.verify(response, Mockito.never()).sendError(Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void testPreHandle_missingAuthorization_returns401() throws Exception {
        assertFalse(new ManagementAuthenticationInterceptor(TOKEN).preHandle(request, response, null));
        Mockito.verify(response).setHeader("WWW-Authenticate", "Bearer realm=\"management\"");
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testPreHandle_wrongToken_returns401() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer not-the-token");

        assertFalse(new ManagementAuthenticationInterceptor(TOKEN).preHandle(request, response, null));
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testPreHandle_nonBearerScheme_returns401() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn("Basic " + TOKEN);

        assertFalse(new ManagementAuthenticationInterceptor(TOKEN).preHandle(request, response, null));
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testPreHandle_tokenNotConfigured_returns403() throws Exception {
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer ");

        assertFalse(new ManagementAuthenticationInterceptor("").preHandle(request, response, null));
        assertFalse(new ManagementAuthenticationInterceptor(null).preHandle(request, response, null));
        Mockito.verify(response, Mockito.times(2))
            .sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
    }
}
