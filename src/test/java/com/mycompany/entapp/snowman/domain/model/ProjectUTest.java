/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ProjectUTest {

    @Test
    public void gettersAndSetters_storeValues() {
        Date started = new DateTime(2018, 1, 1, 0, 0).toDate();
        Date ended = new DateTime(2020, 1, 1, 0, 0).toDate();
        Client client = new Client();
        client.setId(3);

        Project project = new Project();
        project.setId(5);
        project.setProjectTitle("Phoenix");
        project.setDateStarted(started);
        project.setDateEnded(ended);
        project.setClient(client);

        assertEquals(5, project.getId());
        assertEquals("Phoenix", project.getProjectTitle());
        assertEquals(started, project.getDateStarted());
        assertEquals(ended, project.getDateEnded());
        assertEquals(client, project.getClient());
    }

    @Test
    public void equalsAndHashCode_followContract() {
        Project a = new Project();
        a.setId(1);
        a.setProjectTitle("Phoenix");

        Project b = new Project();
        b.setId(1);
        b.setProjectTitle("Phoenix");

        assertTrue(a.equals(b));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("not a project"));

        b.setProjectTitle("Other");
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        Project project = new Project();
        project.setId(1);
        assertNotNull(project.toString());
    }
}
