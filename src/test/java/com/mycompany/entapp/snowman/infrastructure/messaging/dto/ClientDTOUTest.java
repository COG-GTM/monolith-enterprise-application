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
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class ClientDTOUTest {

    @Test
    public void testEqualsSymmetry() {
        ClientDTO dto1 = createClientDTO(1, "Client A");
        ClientDTO dto2 = createClientDTO(1, "Client A");

        assertTrue(dto1.equals(dto2));
        assertTrue(dto2.equals(dto1));
    }

    @Test
    public void testEqualsConsistency() {
        ClientDTO dto1 = createClientDTO(1, "Client A");
        ClientDTO dto2 = createClientDTO(1, "Client A");

        assertTrue(dto1.equals(dto2));
        assertTrue(dto1.equals(dto2));
    }

    @Test
    public void testNotEquals() {
        ClientDTO dto1 = createClientDTO(1, "Client A");
        ClientDTO dto2 = createClientDTO(2, "Client B");

        assertNotEquals(dto1, dto2);
    }

    @Test
    public void testHashCodeConsistency() {
        ClientDTO dto1 = createClientDTO(1, "Client A");
        ClientDTO dto2 = createClientDTO(1, "Client A");

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    public void testSerializableRoundTrip() throws Exception {
        ClientDTO original = createClientDTO(1, "Client A");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.flush();
        byte[] bytes = bos.toByteArray();

        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        ClientDTO deserialized = (ClientDTO) ois.readObject();

        assertEquals(original, deserialized);
    }

    private ClientDTO createClientDTO(int id, String name) {
        ClientDTO dto = new ClientDTO();
        dto.setClientId(id);
        dto.setClientName(name);
        Set<ProjectDTO> projectDTOS = new HashSet<ProjectDTO>();
        dto.setProjectDTOS(projectDTOS);
        return dto;
    }
}
