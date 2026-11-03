/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiTokenAuthInterceptorUTest {

    private final ApiTokenAuthInterceptor classUnderTest = new ApiTokenAuthInterceptor("rw-token", "ro-token");

    @Test
    public void testMissingAuthorizationHeaderIsUnauthorized() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(classUnderTest.preHandle(request("GET", null), response, null));
        assertEquals(401, response.getStatus());
        assertEquals("Bearer realm=\"snowman\"", response.getHeader("WWW-Authenticate"));
    }

    @Test
    public void testWrongTokenIsUnauthorized() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(classUnderTest.preHandle(request("GET", "Bearer rw-token-x"), response, null));
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testNonBearerSchemeIsUnauthorized() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(classUnderTest.preHandle(request("GET", "Basic rw-token"), response, null));
        assertEquals(401, response.getStatus());
    }

    @Test
    public void testReadWriteTokenAllowsWrites() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(classUnderTest.preHandle(request("DELETE", "Bearer rw-token"), response, null));
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testReadOnlyTokenAllowsGet() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(classUnderTest.preHandle(request("GET", "bearer ro-token"), response, null));
    }

    @Test
    public void testReadOnlyTokenCannotWrite() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(classUnderTest.preHandle(request("POST", "Bearer ro-token"), response, null));
        assertEquals(403, response.getStatus());
    }

    @Test
    public void testNoConfiguredTokensRejectsEverything() throws Exception {
        ApiTokenAuthInterceptor unconfigured = new ApiTokenAuthInterceptor("", "  ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(unconfigured.preHandle(request("GET", "Bearer "), response, null));
        assertEquals(401, response.getStatus());
    }

    private static MockHttpServletRequest request(String method, String authorization) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/project/1");
        if (authorization != null) {
            request.addHeader("Authorization", authorization);
        }
        return request;
    }
}
