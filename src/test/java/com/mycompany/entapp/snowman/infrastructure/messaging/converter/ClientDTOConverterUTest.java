/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ClientDTO;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ClientDTOConverterUTest {

    @Test
    public void testConvertToClientDTO() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Test Client");
        client.setProjects(Collections.<Project>emptySet());

        ClientDTO result = ClientDTOConverter.convertToClientDTO(client);

        assertNotNull(result);
        assertEquals(1, result.getClientId());
        assertEquals("Test Client", result.getClientName());
        assertNotNull(result.getProjectDTOS());
        assertTrue(result.getProjectDTOS().isEmpty());
    }

    @Test
    public void testConvertToClientDTOWithProjects() {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Project 1");

        Set<Project> projects = new HashSet<>();
        projects.add(project);

        Client client = new Client();
        client.setId(2);
        client.setClientName("Client With Projects");
        client.setProjects(projects);

        ClientDTO result = ClientDTOConverter.convertToClientDTO(client);

        assertNotNull(result);
        assertEquals(2, result.getClientId());
        assertEquals("Client With Projects", result.getClientName());
        assertNotNull(result.getProjectDTOS());
        assertEquals(1, result.getProjectDTOS().size());
    }
}
