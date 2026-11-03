/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Enforces {@link RequiresRole} on handler methods using HTTP Basic credentials checked against the configured
 * {@link ApiAccount}s. Missing or invalid credentials get 401, an authenticated caller without the role gets 403.
 * With no enabled accounts every protected request is rejected.
 */
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoleAuthorizationInterceptor.class);

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String BASIC_PREFIX = "basic ";
    static final String CHALLENGE = "Basic realm=\"snowman\", charset=\"UTF-8\"";

    private final List<ApiAccount> accounts;

    public RoleAuthorizationInterceptor(List<ApiAccount> accounts) {
        List<ApiAccount> enabledAccounts = new ArrayList<ApiAccount>();
        if (accounts != null) {
            for (ApiAccount account : accounts) {
                if (account != null && account.isEnabled()) {
                    enabledAccounts.add(account);
                }
            }
        }
        if (enabledAccounts.isEmpty()) {
            LOGGER.warn("No API accounts configured: all role-protected endpoints will reject requests");
        }
        this.accounts = Collections.unmodifiableList(enabledAccounts);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws IOException {
        RequiresRole requirement = findRequirement(handler);
        if (requirement == null) {
            return true;
        }

        ApiRole callerRole = authenticate(request.getHeader("Authorization"));
        if (callerRole == null) {
            response.setHeader("WWW-Authenticate", CHALLENGE);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        if (!callerRole.grants(requirement.value())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        // no-op
    }

    static RequiresRole findRequirement(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresRole requirement = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), RequiresRole.class);
        if (requirement == null) {
            requirement = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), RequiresRole.class);
        }
        return requirement;
    }

    ApiRole authenticate(String authorizationHeader) {
        if (authorizationHeader == null
            || authorizationHeader.length() <= BASIC_PREFIX.length()
            || !authorizationHeader.regionMatches(true, 0, BASIC_PREFIX, 0, BASIC_PREFIX.length())) {
            return null;
        }

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authorizationHeader.substring(BASIC_PREFIX.length()).trim()),
                UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }

        int separator = decoded.indexOf(':');
        if (separator < 0) {
            return null;
        }
        String username = decoded.substring(0, separator);
        String password = decoded.substring(separator + 1);

        ApiRole matchedRole = null;
        for (ApiAccount account : accounts) {
            if (account.matches(username, password) && matchedRole == null) {
                matchedRole = account.getRole();
            }
        }
        return matchedRole;
    }
}
