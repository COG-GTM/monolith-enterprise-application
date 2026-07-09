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

import static org.junit.Assert.*;

public class ClientDTOConverterUTest {

    @Test
    public void givenClient_whenConvertToClientDTO_thenReturnClientDTO() {
        Client client = new Client();
        client.setId(7);
        client.setClientName("Acme");

        Project project = ProjectTestHelper.getProject();
        Set<Project> projects = new HashSet<>();
        projects.add(project);
        client.setProjects(projects);

        ClientDTO clientDTO = ClientDTOConverter.convertToClientDTO(client);

        assertEquals(7, clientDTO.getClientId());
        assertEquals("Acme", clientDTO.getClientName());
        assertEquals(1, clientDTO.getProjectDTOS().size());
        assertEquals(project.getProjectTitle(),
            clientDTO.getProjectDTOS().iterator().next().getProjectTitle());
    }
}
