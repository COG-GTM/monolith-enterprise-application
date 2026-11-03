/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.UserRestEndpoint;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.Base64Utils;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AdminAuthorizationInterceptorUTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HandlerMethod deleteUserHandler;
    private HandlerMethod getUserHandler;

    @Before
    public void setUp() throws Exception {
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        UserRestEndpoint endpoint = new UserRestEndpoint();
        deleteUserHandler = new HandlerMethod(endpoint, "deleteUser", Integer.class);
        getUserHandler = new HandlerMethod(endpoint, "getUser", String.class);
    }

    @Test
    public void adminOnlyHandlerWithoutCredentialsIsRejectedWith401() throws Exception {
        AdminAuthorizationInterceptor interceptor = interceptor("admin", "s3cret");

        assertFalse(interceptor.preHandle(request, response, deleteUserHandler));

        Mockito.verify(response).setHeader(Mockito.eq("WWW-Authenticate"), Mockito.startsWith("Basic "));
        Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
    }

    @Test
    public void adminOnlyHandlerWithWrongPasswordIsRejectedWith401() throws Exception {
        AdminAuthorizationInterceptor interceptor = interceptor("admin", "s3cret");
        Mockito.when(request.getHeader("Authorization")).thenReturn(basic("admin", "admin"));

        assertFalse(interceptor.preHandle(request, response, deleteUserHandler));

        Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
    }

    @Test
    public void adminOnlyHandlerWithMalformedHeaderIsRejectedWith401() throws Exception {
        AdminAuthorizationInterceptor interceptor = interceptor("admin", "s3cret");
        Mockito.when(request.getHeader("Authorization")).thenReturn("Basic !!!not-base64");

        assertFalse(interceptor.preHandle(request, response, deleteUserHandler));

        Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_UNAUTHORIZED), Mockito.anyString());
    }

    @Test
    public void adminOnlyHandlerWithAdminCredentialsIsAllowed() throws Exception {
        AdminAuthorizationInterceptor interceptor = interceptor("admin", "s3cret");
        Mockito.when(request.getHeader("Authorization")).thenReturn(basic("admin", "s3cret"));

        assertTrue(interceptor.preHandle(request, response, deleteUserHandler));

        Mockito.verifyZeroInteractions(response);
    }

    @Test
    public void adminOnlyHandlerIsRejectedWith403WhenAdminIsNotConfigured() throws Exception {
        AdminAuthorizationInterceptor interceptor = interceptor("", "");
        Mockito.when(request.getHeader("Authorization")).thenReturn(basic("", ""));

        assertFalse(interceptor.preHandle(request, response, deleteUserHandler));

        Mockito.verify(response).sendError(Mockito.eq(HttpServletResponse.SC_FORBIDDEN), Mockito.anyString());
    }

    @Test
    public void handlerWithoutAdminOnlyIsNotAffected() throws Exception {
        AdminAuthorizationInterceptor interceptor = interceptor("admin", "s3cret");

        assertTrue(interceptor.preHandle(request, response, getUserHandler));

        Mockito.verifyZeroInteractions(response);
    }

    private static AdminAuthorizationInterceptor interceptor(String username, String password) {
        return new AdminAuthorizationInterceptor(new AdminCredentials(username, password));
    }

    private static String basic(String username, String password) {
        return "Basic " + Base64Utils.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
