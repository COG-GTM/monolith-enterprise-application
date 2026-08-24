/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.repository.impl;

import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.repository.ProjectRepository;
import com.mycompany.entapp.snowman.infrastructure.db.dao.ProjectDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProjectRepositoryImpl implements ProjectRepository {

    @Autowired
    private ProjectDao projectDao;

    @Transactional(readOnly = true)
    @Override
    public Project findProject(int projectId) {
        return projectDao.retrieveProject(projectId);
    }

    @Transactional
    @Override
    public void saveProject(Project project) {
        projectDao.saveProject(project);
    }

    @Transactional
    @Override
    public void removeProject(int projectId) {
        projectDao.removeProject(projectId);
    }
}
