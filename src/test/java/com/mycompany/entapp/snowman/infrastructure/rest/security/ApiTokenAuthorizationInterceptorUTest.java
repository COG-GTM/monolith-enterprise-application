/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import com.mycompany.entapp.snowman.infrastructure.management.HealthCheckRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.ClientRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ApiTokenAuthorizationInterceptorUTest {

    private static final String ADMIN_TOKEN = "admin-token-0123456789abcdef0123456789";
    private static final String READER_TOKEN = "reader-token-0123456789abcdef012345678";

    private ApiTokenAuthorizationInterceptor classUnderTest;
    private HttpServletResponse response;

    @Before
    public void setUp() {
        classUnderTest = new ApiTokenAuthorizationInterceptor();
        classUnderTest.setAdminToken(ADMIN_TOKEN);
        classUnderTest.setReaderToken(READER_TOKEN);
        response = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void testDeleteWithoutTokenIsUnauthorized() throws Exception {
        assertFalse(classUnderTest.preHandle(request("DELETE", null), response, deleteHandler()));

        Mockito.verify(response).setHeader("WWW-Authenticate", "Bearer realm=\"snowman\"");
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testCreateAndUpdateWithoutTokenAreUnauthorized() throws Exception {
        assertFalse(classUnderTest.preHandle(request("POST", null), response, createHandler()));
        assertFalse(classUnderTest.preHandle(request("POST", null), response, updateHandler()));

        Mockito.verify(response, Mockito.times(2)).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testDeleteWithUnknownTokenIsUnauthorized() throws Exception {
        assertFalse(classUnderTest.preHandle(request("DELETE", "Bearer " + ADMIN_TOKEN + "x"), response, deleteHandler()));

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testNonBearerSchemeIsUnauthorized() throws Exception {
        assertFalse(classUnderTest.preHandle(request("DELETE", "Basic " + ADMIN_TOKEN), response, deleteHandler()));
        assertFalse(classUnderTest.preHandle(request("DELETE", ADMIN_TOKEN), response, deleteHandler()));
        assertFalse(classUnderTest.preHandle(request("DELETE", "Bearer"), response, deleteHandler()));

        Mockito.verify(response, Mockito.times(3)).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testReaderTokenCannotMutateClients() throws Exception {
        String header = "Bearer " + READER_TOKEN;

        assertFalse(classUnderTest.preHandle(request("POST", header), response, createHandler()));
        assertFalse(classUnderTest.preHandle(request("POST", header), response, updateHandler()));
        assertFalse(classUnderTest.preHandle(request("DELETE", header), response, deleteHandler()));

        Mockito.verify(response, Mockito.times(3)).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    public void testAdminTokenCanMutateClients() throws Exception {
        String header = "bearer " + ADMIN_TOKEN;

        assertTrue(classUnderTest.preHandle(request("POST", header), response, createHandler()));
        assertTrue(classUnderTest.preHandle(request("POST", header), response, updateHandler()));
        assertTrue(classUnderTest.preHandle(request("DELETE", header), response, deleteHandler()));

        Mockito.verifyZeroInteractions(response);
    }

    @Test
    public void testReadRequiresReaderOrAdminToken() throws Exception {
        assertFalse(classUnderTest.preHandle(request("GET", null), response, getHandler()));
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);

        assertTrue(classUnderTest.preHandle(request("GET", "Bearer " + READER_TOKEN), response, getHandler()));
        assertTrue(classUnderTest.preHandle(request("GET", "Bearer " + ADMIN_TOKEN), response, getHandler()));
    }

    @Test
    public void testUnconfiguredTokensFailClosed() throws Exception {
        ApiTokenAuthorizationInterceptor unconfigured = new ApiTokenAuthorizationInterceptor();
        unconfigured.setAdminToken("");
        unconfigured.setReaderToken(null);

        assertFalse(unconfigured.preHandle(request("DELETE", "Bearer "), response, deleteHandler()));
        assertFalse(unconfigured.preHandle(request("DELETE", "Bearer " + ADMIN_TOKEN), response, deleteHandler()));

        Mockito.verify(response, Mockito.times(2)).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testShortTokensAreIgnored() throws Exception {
        ApiTokenAuthorizationInterceptor weak = new ApiTokenAuthorizationInterceptor();
        weak.setAdminToken("admin");

        assertFalse(weak.preHandle(request("DELETE", "Bearer admin"), response, deleteHandler()));

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testIdenticalAdminAndReaderTokensFailClosed() throws Exception {
        ApiTokenAuthorizationInterceptor misconfigured = new ApiTokenAuthorizationInterceptor();
        misconfigured.setAdminToken(ADMIN_TOKEN);
        misconfigured.setReaderToken(ADMIN_TOKEN);

        assertFalse(misconfigured.preHandle(request("DELETE", "Bearer " + ADMIN_TOKEN), response, deleteHandler()));

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testUnannotatedHandlersAreNotAffected() throws Exception {
        HandlerMethod health = new HandlerMethod(new HealthCheckRestEndpoint(), "checkStatus");

        assertTrue(classUnderTest.preHandle(request("GET", null), response, health));
        assertTrue(classUnderTest.preHandle(request("GET", null), response, new Object()));

        Mockito.verifyZeroInteractions(response);
    }

    @Test
    public void testEveryMutatingClientEndpointRequiresAdmin() {
        for (Method method : ClientRestEndpoint.class.getDeclaredMethods()) {
            RequestMapping mapping = method.getAnnotation(RequestMapping.class);
            if (mapping == null || Arrays.equals(mapping.method(), new RequestMethod[]{RequestMethod.GET})) {
                continue;
            }
            RequiresApiRole requirement = method.getAnnotation(RequiresApiRole.class);
            assertNotNull(method.getName() + " must declare @RequiresApiRole", requirement);
            assertEquals(method.getName(), ApiRole.ADMIN, requirement.value());
        }
    }

    private static HttpServletRequest request(String method, String authorization) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getMethod()).thenReturn(method);
        Mockito.when(request.getHeader("Authorization")).thenReturn(authorization);
        return request;
    }

    private static HandlerMethod getHandler() throws NoSuchMethodException {
        return new HandlerMethod(new ClientRestEndpoint(), "getClientInfo", Integer.class);
    }

    private static HandlerMethod createHandler() throws NoSuchMethodException {
        return new HandlerMethod(new ClientRestEndpoint(), "createClientInfo", ClientResource.class);
    }

    private static HandlerMethod updateHandler() throws NoSuchMethodException {
        return new HandlerMethod(new ClientRestEndpoint(), "updateClientInfo", ClientResource.class);
    }

    private static HandlerMethod deleteHandler() throws NoSuchMethodException {
        return new HandlerMethod(new ClientRestEndpoint(), "deleteClientInfo", Integer.class);
    }
}
