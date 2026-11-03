/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * Requires HTTP Basic administrator credentials for handlers annotated with {@link AdminOnly}.
 * The check is bound to the resolved handler method, so it cannot be bypassed by URL variants.
 */
@Component
public class AdminAuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAuthorizationInterceptor.class);

    private static final String BASIC_PREFIX = "Basic ";
    private static final String CHALLENGE = "Basic realm=\"snowman-admin\", charset=\"UTF-8\"";

    private final AdminCredentials adminCredentials;

    @Autowired
    public AdminAuthorizationInterceptor(AdminCredentials adminCredentials) {
        this.adminCredentials = adminCredentials;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!requiresAdmin(handler)) {
            return true;
        }

        if (!adminCredentials.isConfigured()) {
            LOGGER.warn("Refused {} {}: administrator credentials are not configured",
                request.getMethod(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Administrator access is disabled");
            return false;
        }

        String[] credentials = parseBasicCredentials(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (credentials == null || !adminCredentials.matches(credentials[0], credentials[1])) {
            LOGGER.warn("Refused unauthenticated {} {}", request.getMethod(), request.getRequestURI());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, CHALLENGE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Administrator authentication required");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
    }

    private static boolean requiresAdmin(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        return AnnotationUtils.findAnnotation(handlerMethod.getMethod(), AdminOnly.class) != null
            || AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), AdminOnly.class) != null;
    }

    private static String[] parseBasicCredentials(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            return null;
        }
        String decoded;
        try {
            decoded = new String(Base64Utils.decodeFromString(authorization.substring(BASIC_PREFIX.length()).trim()),
                StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
        int separator = decoded.indexOf(':');
        if (separator < 0) {
            return null;
        }
        return new String[]{decoded.substring(0, separator), decoded.substring(separator + 1)};
    }
}
