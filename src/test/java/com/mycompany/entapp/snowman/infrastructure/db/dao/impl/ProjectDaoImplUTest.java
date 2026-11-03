/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.infrastructure.db.dao.impl;

import com.mycompany.entapp.snowman.domain.model.Project;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDaoImplUTest {

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Query query;

    @InjectMocks
    private ProjectDaoImpl classUnderTest;

    @Before
    public void setUp() {
        Mockito.when(sessionFactory.getCurrentSession()).thenReturn(session);
        Mockito.when(session.createQuery(Matchers.anyString())).thenReturn(query);
        Mockito.when(query.setParameter(Matchers.anyString(), Matchers.any())).thenReturn(query);
    }

    @Test
    public void testRemoveProjectDeletesTheProjectEntity() {
        Project project = new Project();
        project.setId(5);
        Mockito.when(session.load(Project.class, 5)).thenReturn(project);

        classUnderTest.removeProject(5);

        InOrder inOrder = Mockito.inOrder(session, query);
        inOrder.verify(session).createQuery("delete from EmployeeProject ep where ep.project.id = :projectId");
        inOrder.verify(query).setParameter("projectId", 5);
        inOrder.verify(query).executeUpdate();
        inOrder.verify(session).delete(project);
    }
}
