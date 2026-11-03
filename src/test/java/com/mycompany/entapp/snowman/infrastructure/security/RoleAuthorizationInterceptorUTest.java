/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

import com.mycompany.entapp.snowman.infrastructure.management.HealthCheckRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.endpoint.EmployeeRestEndpoint;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.EmployeeResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;

import static org.junit.Assert.*;

public class RoleAuthorizationInterceptorUTest {

    private static final String ADMIN_USER = "admin-user";
    private static final String ADMIN_PASSWORD = "admin-pass:with-colon";
    private static final String VIEWER_USER = "viewer-user";
    private static final String VIEWER_PASSWORD = "viewer-pass";

    private RoleAuthorizationInterceptor systemUnderTest;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void setUp() {
        systemUnderTest = new RoleAuthorizationInterceptor(Arrays.asList(
            new ApiAccount(ADMIN_USER, ADMIN_PASSWORD, ApiRole.ADMIN),
            new ApiAccount(VIEWER_USER, VIEWER_PASSWORD, ApiRole.VIEWER)));
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
    }

    @Test
    public void givenNoCredentials_whenCreateEmployee_thenReturn401WithChallenge() throws Exception {
        assertFalse(systemUnderTest.preHandle(request, response, employeeHandler("createEmployee")));

        Mockito.verify(response).setHeader("WWW-Authenticate", RoleAuthorizationInterceptor.CHALLENGE);
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void givenNoCredentials_whenAnyEmployeeEndpoint_thenReturn401() throws Exception {
        for (HandlerMethod handler : allEmployeeHandlers()) {
            HttpServletResponse handlerResponse = Mockito.mock(HttpServletResponse.class);

            assertFalse(handler.getMethod().getName(), systemUnderTest.preHandle(request, handlerResponse, handler));

            Mockito.verify(handlerResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    public void givenWrongPassword_whenDeleteEmployee_thenReturn401() throws Exception {
        givenAuthorization(basic(ADMIN_USER, "wrong"));

        assertFalse(systemUnderTest.preHandle(request, response, employeeHandler("deleteExistingEmployee")));

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void givenMalformedAuthorization_whenGetEmployee_thenReturn401() throws Exception {
        for (String header : Arrays.asList("Bearer abc", "Basic", "Basic !!!not-base64!!!",
            "Basic " + Base64.getEncoder().encodeToString("no-colon".getBytes(Charset.forName("UTF-8"))))) {
            HttpServletResponse headerResponse = Mockito.mock(HttpServletResponse.class);
            givenAuthorization(header);

            assertFalse(header, systemUnderTest.preHandle(request, headerResponse, employeeHandler("getEmployee")));

            Mockito.verify(headerResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    @Test
    public void givenViewer_whenMutatingEmployee_thenReturn403() throws Exception {
        givenAuthorization(basic(VIEWER_USER, VIEWER_PASSWORD));

        for (String method : Arrays.asList("createEmployee", "updateExistingEmployee", "deleteExistingEmployee")) {
            HttpServletResponse methodResponse = Mockito.mock(HttpServletResponse.class);

            assertFalse(method, systemUnderTest.preHandle(request, methodResponse, employeeHandler(method)));

            Mockito.verify(methodResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Test
    public void givenViewer_whenGetEmployee_thenAllow() throws Exception {
        givenAuthorization(basic(VIEWER_USER, VIEWER_PASSWORD));

        assertTrue(systemUnderTest.preHandle(request, response, employeeHandler("getEmployee")));

        Mockito.verifyZeroInteractions(response);
    }

    @Test
    public void givenAdmin_whenAnyEmployeeEndpoint_thenAllow() throws Exception {
        givenAuthorization("basic " + Base64.getEncoder().encodeToString(
            (ADMIN_USER + ":" + ADMIN_PASSWORD).getBytes(Charset.forName("UTF-8"))));

        for (HandlerMethod handler : allEmployeeHandlers()) {
            assertTrue(handler.getMethod().getName(), systemUnderTest.preHandle(request, response, handler));
        }
        Mockito.verifyZeroInteractions(response);
    }

    @Test
    public void givenNoAccountsConfigured_whenAdminCredentialsSent_thenReturn401() throws Exception {
        systemUnderTest = new RoleAuthorizationInterceptor(Arrays.asList(
            new ApiAccount("", "", ApiRole.ADMIN),
            new ApiAccount(null, null, ApiRole.VIEWER)));
        givenAuthorization(basic("", ""));

        assertFalse(systemUnderTest.preHandle(request, response, employeeHandler("getEmployee")));

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void givenEmptyAccountList_whenGetEmployee_thenReturn401() throws Exception {
        systemUnderTest = new RoleAuthorizationInterceptor(Collections.<ApiAccount>emptyList());
        givenAuthorization(basic(ADMIN_USER, ADMIN_PASSWORD));

        assertFalse(systemUnderTest.preHandle(request, response, employeeHandler("getEmployee")));

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void givenUnannotatedEndpoint_whenNoCredentials_thenAllow() throws Exception {
        HandlerMethod health = new HandlerMethod(new HealthCheckRestEndpoint(),
            HealthCheckRestEndpoint.class.getMethod("checkStatus"));

        assertTrue(systemUnderTest.preHandle(request, response, health));
        assertTrue(systemUnderTest.preHandle(request, response, new Object()));
        Mockito.verifyZeroInteractions(response);
    }

    private void givenAuthorization(String header) {
        Mockito.when(request.getHeader("Authorization")).thenReturn(header);
    }

    private static String basic(String username, String password) {
        return "Basic " + Base64.getEncoder().encodeToString(
            (username + ":" + password).getBytes(Charset.forName("UTF-8")));
    }

    private static HandlerMethod[] allEmployeeHandlers() throws NoSuchMethodException {
        return new HandlerMethod[]{
            employeeHandler("getEmployee"),
            employeeHandler("createEmployee"),
            employeeHandler("updateExistingEmployee"),
            employeeHandler("deleteExistingEmployee")
        };
    }

    private static HandlerMethod employeeHandler(String name) throws NoSuchMethodException {
        Class<?> parameterType = "createEmployee".equals(name) || "updateExistingEmployee".equals(name)
            ? EmployeeResource.class : Integer.class;
        return new HandlerMethod(new EmployeeRestEndpoint(), EmployeeRestEndpoint.class.getMethod(name, parameterType));
    }
}
