/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.security.MessageDigest;

/**
 * Requires an {@code Authorization: Bearer <token>} header on every request it is mapped to.
 * The read-write token grants every method; the read-only token grants GET/HEAD only (403 otherwise).
 * A blank token disables that access level, so with neither configured all requests get 401.
 */
public class ApiTokenAuthInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenAuthInterceptor.class);

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String CHALLENGE = "Bearer realm=\"snowman\"";

    private final byte[] readWriteToken;
    private final byte[] readOnlyToken;

    public ApiTokenAuthInterceptor(String readWriteToken, String readOnlyToken) {
        this.readWriteToken = toBytes(readWriteToken);
        this.readOnlyToken = toBytes(readOnlyToken);
        if (this.readWriteToken == null && this.readOnlyToken == null) {
            LOGGER.warn("No REST API token configured (SNOWMAN_API_TOKEN / SNOWMAN_API_READONLY_TOKEN); "
                + "protected endpoints will reject every request");
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        byte[] presented = bearerToken(request);

        if (matches(presented, readWriteToken)) {
            return true;
        }

        if (matches(presented, readOnlyToken)) {
            if (isReadOnly(request.getMethod())) {
                return true;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, CHALLENGE);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
    }

    private static boolean isReadOnly(String method) {
        return "GET".equals(method) || "HEAD".equals(method);
    }

    private static byte[] bearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        return toBytes(header.substring(BEARER_PREFIX.length()));
    }

    private static boolean matches(byte[] presented, byte[] expected) {
        return presented != null && expected != null && MessageDigest.isEqual(presented, expected);
    }

    private static byte[] toBytes(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        return token.trim().getBytes(UTF_8);
    }
}
