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

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ClientDTOConverterUTest {

    @Test
    public void convertToClientDTO_copiesFieldsAndConvertsProjects() {
        Client client = new Client();
        client.setId(11);
        client.setClientName("ACME");

        Set<Project> projects = new HashSet<Project>();
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Phoenix");
        projects.add(project);
        client.setProjects(projects);

        ClientDTO dto = ClientDTOConverter.convertToClientDTO(client);

        assertNotNull(dto);
        assertEquals(11, dto.getClientId());
        assertEquals("ACME", dto.getClientName());
        assertNotNull(dto.getProjectDTOS());
        assertEquals(1, dto.getProjectDTOS().size());
    }
}
