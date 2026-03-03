/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ClientUTest {

    @Test
    public void testGettersAndSetters() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Test Client");
        Set<Project> projects = new HashSet<>();
        client.setProjects(projects);

        assertEquals(1, client.getId());
        assertEquals("Test Client", client.getClientName());
        assertEquals(projects, client.getProjects());
    }

    @Test
    public void testEqualsAndHashCode_SameObject() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Client A");

        assertTrue(client.equals(client));
    }

    @Test
    public void testEqualsAndHashCode_EqualObjects() {
        Client client1 = new Client();
        client1.setId(1);
        client1.setClientName("Client A");

        Client client2 = new Client();
        client2.setId(1);
        client2.setClientName("Client A");

        assertTrue(client1.equals(client2));
        assertTrue(client2.equals(client1));
        assertEquals(client1.hashCode(), client2.hashCode());
    }

    @Test
    public void testEqualsAndHashCode_DifferentId() {
        Client client1 = new Client();
        client1.setId(1);
        client1.setClientName("Client A");

        Client client2 = new Client();
        client2.setId(2);
        client2.setClientName("Client A");

        assertFalse(client1.equals(client2));
    }

    @Test
    public void testEqualsAndHashCode_DifferentName() {
        Client client1 = new Client();
        client1.setId(1);
        client1.setClientName("Client A");

        Client client2 = new Client();
        client2.setId(1);
        client2.setClientName("Client B");

        assertFalse(client1.equals(client2));
    }

    @Test
    public void testEquals_Null() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Client A");

        assertFalse(client.equals(null));
    }

    @Test
    public void testEquals_DifferentType() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Client A");

        assertFalse(client.equals("not a client"));
    }

    @Test
    public void testToString() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Client A");

        String result = client.toString();
        assertNotNull(result);
        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("clientName=Client A"));
    }
}
