/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ClientDTO;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ProjectDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ProjectDTOConverter.class})
public class ClientDTOConverterUTest {

    @Test
    public void testConvertToClientDTO() {
        Client client = new Client();
        client.setId(1);
        client.setClientName("Test Client");
        client.setProjects(Collections.<com.mycompany.entapp.snowman.domain.model.Project>emptySet());

        Set<ProjectDTO> expectedProjectDTOS = new HashSet<ProjectDTO>();
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectId(10);
        projectDTO.setProjectTitle("Mocked Project");
        expectedProjectDTOS.add(projectDTO);

        PowerMockito.mockStatic(ProjectDTOConverter.class);
        PowerMockito.when(ProjectDTOConverter.convertToProjectDTOS(client.getProjects())).thenReturn(expectedProjectDTOS);

        ClientDTO clientDTO = ClientDTOConverter.convertToClientDTO(client);

        assertEquals(1, clientDTO.getClientId());
        assertEquals("Test Client", clientDTO.getClientName());
        assertEquals(expectedProjectDTOS, clientDTO.getProjectDTOS());
    }
}
