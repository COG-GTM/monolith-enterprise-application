/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class UserResourceUTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void passwordIsNeverSerialized() throws Exception {
        UserResource userResource = new UserResource();
        userResource.setUserId(1);
        userResource.setUsername("username");
        userResource.setPassword("password");
        userResource.setEmail("username@email.com");

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(userResource));

        assertFalse(json.has("password"));
        assertEquals("username", json.get("username").asText());
        assertEquals("username@email.com", json.get("email").asText());
    }

    @Test
    public void passwordIsAcceptedOnInput() throws Exception {
        UserResource userResource = objectMapper.readValue(
            "{\"userId\":5,\"username\":\"new\",\"password\":\"s3cret\"}", UserResource.class);

        assertEquals("s3cret", userResource.getPassword());
    }
}
