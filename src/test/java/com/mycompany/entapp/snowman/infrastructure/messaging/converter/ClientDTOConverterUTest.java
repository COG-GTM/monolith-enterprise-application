/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.ProjectTestHelper;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ClientDTO;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ClientDTOConverterUTest {

    @Test
    public void testConvertToClientDTO() {
        Project project = ProjectTestHelper.getProject();
        Set<Project> projects = new HashSet<>();
        projects.add(project);

        Client client = new Client();
        client.setId(1);
        client.setClientName("Client");
        client.setProjects(projects);

        ClientDTO clientDTO = ClientDTOConverter.convertToClientDTO(client);

        assertEquals(1, clientDTO.getClientId());
        assertEquals("Client", clientDTO.getClientName());
        assertEquals(1, clientDTO.getProjectDTOS().size());
        assertEquals(project.getProjectTitle(), clientDTO.getProjectDTOS().iterator().next().getProjectTitle());
    }
}
