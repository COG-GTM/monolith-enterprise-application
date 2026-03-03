/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain;

import com.mycompany.entapp.snowman.domain.model.Project;

import java.time.LocalDate;

public final class ProjectTestHelper {

    private ProjectTestHelper(){
    }

    public static Project getProject() {
        Project project = new Project();
        project.setId(1);
        project.setProjectTitle("Project");
        // Java 8: Migrated from Joda-Time DateTime to java.time.LocalDate
        project.setDateStarted(LocalDate.of(2018, 1, 1));
        project.setDateEnded(LocalDate.of(2020, 1, 1));
        return project;
    }
}
