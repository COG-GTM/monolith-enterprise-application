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
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class EmployeeDTOUTest {

    @Test
    public void testEqualsSymmetry() {
        EmployeeDTO dto1 = createEmployeeDTO(1, "John", "Doe", "Developer");
        EmployeeDTO dto2 = createEmployeeDTO(1, "John", "Doe", "Developer");

        assertTrue(dto1.equals(dto2));
        assertTrue(dto2.equals(dto1));
    }

    @Test
    public void testEqualsConsistency() {
        EmployeeDTO dto1 = createEmployeeDTO(1, "John", "Doe", "Developer");
        EmployeeDTO dto2 = createEmployeeDTO(1, "John", "Doe", "Developer");

        assertTrue(dto1.equals(dto2));
        assertTrue(dto1.equals(dto2));
    }

    @Test
    public void testNotEquals() {
        EmployeeDTO dto1 = createEmployeeDTO(1, "John", "Doe", "Developer");
        EmployeeDTO dto2 = createEmployeeDTO(2, "Jane", "Smith", "Manager");

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testHashCodeConsistency() {
        EmployeeDTO dto1 = createEmployeeDTO(1, "John", "Doe", "Developer");
        EmployeeDTO dto2 = createEmployeeDTO(1, "John", "Doe", "Developer");

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void testSerializableRoundTrip() throws Exception {
        EmployeeDTO original = createEmployeeDTO(1, "John", "Doe", "Developer");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        EmployeeDTO deserialized = (EmployeeDTO) ois.readObject();

        assertEquals(original, deserialized);
    }

    private EmployeeDTO createEmployeeDTO(int id, String firstName, String surname, String role) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(id);
        dto.setFirstName(firstName);
        dto.setSurname(surname);
        dto.setRole(role);
        List<ProjectDTO> projectDTOList = new ArrayList<ProjectDTO>();
        dto.setProjectDTOList(projectDTOList);
        return dto;
    }
}
