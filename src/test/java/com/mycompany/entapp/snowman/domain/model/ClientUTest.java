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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ClientUTest {

    @Test
    public void gettersAndSetters_storeValues() {
        Client client = new Client();
        client.setId(7);
        client.setClientName("ACME");

        Set<Project> projects = new HashSet<Project>();
        projects.add(new Project());
        client.setProjects(projects);

        assertEquals(7, client.getId());
        assertEquals("ACME", client.getClientName());
        assertEquals(projects, client.getProjects());
    }

    @Test
    public void equalsAndHashCode_basedOnIdAndName() {
        Client a = new Client();
        a.setId(1);
        a.setClientName("ACME");
        a.setProjects(Collections.<Project>emptySet());

        Client b = new Client();
        b.setId(1);
        b.setClientName("ACME");
        b.setProjects(new HashSet<Project>(Collections.singletonList(new Project())));

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("not a client"));

        b.setClientName("Other");
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("ACME");
        assertNotNull(client.toString());
    }
}
