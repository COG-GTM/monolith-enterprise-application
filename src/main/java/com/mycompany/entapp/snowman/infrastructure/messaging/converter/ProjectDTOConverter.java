/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.messaging.converter;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.infrastructure.messaging.dto.ProjectDTO;

import java.util.Set;
import java.util.stream.Collectors;

public final class ProjectDTOConverter {

    private ProjectDTOConverter() {
    }

    public static ProjectDTO convertToProjectDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectId(project.getId());
        projectDTO.setProjectTitle(project.getProjectTitle());
        projectDTO.setDateStarted(project.getDateStarted());
        projectDTO.setDateEnded(project.getDateEnded());
        return projectDTO;
    }

    public static Set<ProjectDTO> convertToProjectDTOS(Set<Project> projects) {
        return projects.stream()
            .map(ProjectDTOConverter::convertToProjectDTO)
            .collect(Collectors.toSet());
    }
}
