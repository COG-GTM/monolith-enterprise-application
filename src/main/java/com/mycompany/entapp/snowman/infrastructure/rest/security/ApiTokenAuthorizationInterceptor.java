/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Authenticates callers of handlers annotated with {@link RequiresApiRole} using a static
 * bearer token per role ({@code Authorization: Bearer <token>}).
 * <p>
 * Missing or unknown tokens get 401, a valid token with an insufficient role gets 403.
 * A role whose token is not configured (or is shorter than {@link #MIN_TOKEN_LENGTH})
 * cannot be authenticated at all, so protected endpoints fail closed.
 * <p>
 * The check is keyed on the resolved handler rather than on a URL pattern, so path
 * variations (double slashes, suffixes, etc.) that still route to a protected handler
 * cannot bypass it.
 */
public class ApiTokenAuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ApiTokenAuthorizationInterceptor.class);

    static final int MIN_TOKEN_LENGTH = 32;

    private static final String BEARER_SCHEME = "Bearer";
    private static final String AUTHENTICATE_CHALLENGE = "Bearer realm=\"snowman\"";
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private byte[] adminTokenDigest;
    private byte[] readerTokenDigest;

    public void setAdminToken(String adminToken) {
        this.adminTokenDigest = digestOrNull("admin", adminToken);
    }

    public void setReaderToken(String readerToken) {
        this.readerTokenDigest = digestOrNull("reader", readerToken);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {

        RequiresApiRole requirement = findRequirement(handler);
        if (requirement == null) {
            return true;
        }

        ApiRole callerRole = authenticate(request.getHeader("Authorization"));
        if (callerRole == null) {
            LOG.warn("Rejected unauthenticated {} request to {}", request.getMethod(), describe(handler));
            response.setHeader("WWW-Authenticate", AUTHENTICATE_CHALLENGE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (!callerRole.satisfies(requirement.value())) {
            LOG.warn("Rejected {} request to {}: role {} lacks {}",
                request.getMethod(), describe(handler), callerRole, requirement.value());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
    }

    private static RequiresApiRole findRequirement(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresApiRole requirement = handlerMethod.getMethodAnnotation(RequiresApiRole.class);
        if (requirement == null) {
            requirement = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresApiRole.class);
        }
        return requirement;
    }

    private ApiRole authenticate(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        if (token == null) {
            return null;
        }
        if (adminTokenDigest != null && readerTokenDigest != null
            && MessageDigest.isEqual(adminTokenDigest, readerTokenDigest)) {
            LOG.error("The admin and reader API tokens are identical; rejecting both until they differ");
            return null;
        }
        byte[] presented = sha256(token);
        if (adminTokenDigest != null && MessageDigest.isEqual(adminTokenDigest, presented)) {
            return ApiRole.ADMIN;
        }
        if (readerTokenDigest != null && MessageDigest.isEqual(readerTokenDigest, presented)) {
            return ApiRole.READER;
        }
        return null;
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }
        String header = authorizationHeader.trim();
        if (header.length() <= BEARER_SCHEME.length()
            || !header.regionMatches(true, 0, BEARER_SCHEME, 0, BEARER_SCHEME.length())
            || header.charAt(BEARER_SCHEME.length()) != ' ') {
            return null;
        }
        String token = header.substring(BEARER_SCHEME.length() + 1).trim();
        return token.isEmpty() ? null : token;
    }

    private static byte[] digestOrNull(String role, String token) {
        if (token == null || token.trim().isEmpty()) {
            LOG.warn("No {} API token configured; endpoints requiring that role will reject every request", role);
            return null;
        }
        if (token.trim().length() < MIN_TOKEN_LENGTH) {
            LOG.error("The {} API token is shorter than {} characters and has been ignored", role, MIN_TOKEN_LENGTH);
            return null;
        }
        return sha256(token.trim());
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static String describe(Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return handlerMethod.getBeanType().getSimpleName() + "#" + handlerMethod.getMethod().getName();
    }
}
