/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain;

import com.mycompany.entapp.snowman.domain.model.Project;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public final class ProjectTestHelper {

    private ProjectTestHelper(){
    }

    public static Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Project");
        project.setDateStarted(Date.from(LocalDateTime.of(2018, 1, 1, 12, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
        project.setDateEnded(Date.from(LocalDateTime.of(2020, 1, 1, 12, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
        return project;
    }
}
