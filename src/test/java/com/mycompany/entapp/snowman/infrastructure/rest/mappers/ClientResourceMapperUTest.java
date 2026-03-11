/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.rest.mappers;

import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ClientResource;
import com.mycompany.entapp.snowman.infrastructure.rest.resources.ProjectResource;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class ClientResourceMapperUTest {

    @Test
    public void testMapToClient() throws Exception {
        int clientId = 1;
        String clientName = "Client";

        ProjectResource projectResource = new ProjectResource();
        projectResource.setProjectId(1);
        projectResource.setTitle("Project");

        List<ProjectResource> projectResources = new ArrayList<>();
        projectResources.add(projectResource);

        ClientResource clientResource = new ClientResource();
        clientResource.setClientId(clientId);
        clientResource.setClientName(clientName);
        clientResource.setProjects(projectResources);

        Client client = ClientResourceMapper.mapToClient(clientResource);

        assertEquals(clientId, client.getId());
        assertEquals(clientName, client.getClientName());
        assertNotNull(client.getProjects());
        assertEquals(1, client.getProjects().size());
    }

    @Test
    public void testMapToClientResource() throws Exception {
        int clientId = 1;
        String clientName = "Client";

        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Project");

        Set<Project> projects = new HashSet<>();
        projects.add(project);

        Client client = new Client();
        client.setId(clientId);
        client.setClientName(clientName);
        client.setProjects(projects);

        ClientResource clientResource = ClientResourceMapper.mapToClientResource(client);

        assertEquals(clientId, clientResource.getClientId());
        assertEquals(clientName, clientResource.getClientName());
        assertNotNull(clientResource.getProjects());
        assertEquals(1, clientResource.getProjects().size());
    }

}
