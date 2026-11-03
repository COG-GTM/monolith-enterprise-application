/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.security;

/**
 * Roles granted to API callers. {@link #ADMIN} implies every permission of {@link #VIEWER}.
 */
public enum ApiRole {
    VIEWER,
    ADMIN;

    public boolean grants(ApiRole required) {
        return this == ADMIN || this == required;
    }
}
