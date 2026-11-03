/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.security;

public enum ApiRole {

    READER,
    ADMIN;

    public boolean satisfies(ApiRole required) {
        return this == ADMIN || this == required;
    }
}
