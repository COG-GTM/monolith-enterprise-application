/*
 * |-------------------------------------------------
 * | Copyright © 2018 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.application.schedule;

import com.mycompany.entapp.snowman.domain.model.AppInfo;
import com.mycompany.entapp.snowman.domain.model.Client;
import com.mycompany.entapp.snowman.domain.model.Employee;
import com.mycompany.entapp.snowman.domain.model.Project;
import com.mycompany.entapp.snowman.domain.model.User;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReportingDataUTest {

    @Test
    public void gettersAndSetters_returnAssignedValues() {
        ReportingData data = new ReportingData();

        List<Client> clients = Collections.singletonList(new Client());
        List<Project> projects = Collections.singletonList(new Project());
        List<Employee> employees = Collections.singletonList(new Employee());
        List<User> users = Collections.singletonList(new User());
        AppInfo appInfo = new AppInfo();
        appInfo.setVersion("1.0.0");

        data.setClients(clients);
        data.setProjects(projects);
        data.setEmployees(employees);
        data.setUsers(users);
        data.setAppInfo(appInfo);

        assertEquals(clients, data.getClients());
        assertEquals(projects, data.getProjects());
        assertEquals(employees, data.getEmployees());
        assertEquals(users, data.getUsers());
        assertEquals(appInfo, data.getAppInfo());
    }

    @Test
    public void equalsAndHashCode_followContract() {
        ReportingData a = new ReportingData();
        ReportingData b = new ReportingData();

        a.setClients(new ArrayList<Client>());
        b.setClients(new ArrayList<Client>());

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.equals(a));
        assertFalse(a.equals(null));
        assertFalse(a.equals("string"));

        b.setUsers(Collections.singletonList(new User()));
        assertFalse(a.equals(b));
    }

    @Test
    public void toString_isNonNull() {
        ReportingData data = new ReportingData();
        assertNotNull(data.toString());
    }
}
