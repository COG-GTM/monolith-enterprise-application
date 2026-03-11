/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.dto;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ProjectDTOUTest {

    @Test
    public void testEqualsSymmetry() {
        Date now = new Date();
        ProjectDTO dto1 = createProjectDTO(1, "Project A", now, now);
        ProjectDTO dto2 = createProjectDTO(1, "Project A", now, now);

        assertTrue(dto1.equals(dto2));
        assertTrue(dto2.equals(dto1));
    }

    @Test
    public void testEqualsConsistency() {
        Date now = new Date();
        ProjectDTO dto1 = createProjectDTO(1, "Project A", now, now);
        ProjectDTO dto2 = createProjectDTO(1, "Project A", now, now);

        assertTrue(dto1.equals(dto2));
        assertTrue(dto1.equals(dto2));
    }

    @Test
    public void testNotEquals() {
        Date now = new Date();
        ProjectDTO dto1 = createProjectDTO(1, "Project A", now, now);
        ProjectDTO dto2 = createProjectDTO(2, "Project B", now, now);

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testHashCodeConsistency() {
        Date now = new Date();
        ProjectDTO dto1 = createProjectDTO(1, "Project A", now, now);
        ProjectDTO dto2 = createProjectDTO(1, "Project A", now, now);

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void testSerializableRoundTrip() throws Exception {
        Date now = new Date();
        ProjectDTO original = createProjectDTO(1, "Project A", now, now);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ProjectDTO deserialized = (ProjectDTO) ois.readObject();

        assertEquals(original, deserialized);
    }

    private ProjectDTO createProjectDTO(int id, String title, Date dateStarted, Date dateEnded) {
        ProjectDTO dto = new ProjectDTO();
        dto.setProjectId(id);
        dto.setProjectTitle(title);
        dto.setDateStarted(dateStarted);
        dto.setDateEnded(dateEnded);
        return dto;
    }
}
