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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReportingDataUTest {

    @Test
    public void testGettersAndSettersClients() {
        ReportingData reportingData = new ReportingData();
        assertNull(reportingData.getClients());

        List<Client> clients = new ArrayList<Client>();
        reportingData.setClients(clients);
        assertEquals(clients, reportingData.getClients());
    }

    @Test
    public void testGettersAndSettersProjects() {
        ReportingData reportingData = new ReportingData();
        assertNull(reportingData.getProjects());

        List<Project> projects = new ArrayList<Project>();
        reportingData.setProjects(projects);
        assertEquals(projects, reportingData.getProjects());
    }

    @Test
    public void testGettersAndSettersEmployees() {
        ReportingData reportingData = new ReportingData();
        assertNull(reportingData.getEmployees());

        List<Employee> employees = new ArrayList<Employee>();
        reportingData.setEmployees(employees);
        assertEquals(employees, reportingData.getEmployees());
    }

    @Test
    public void testGettersAndSettersAppInfo() {
        ReportingData reportingData = new ReportingData();
        assertNull(reportingData.getAppInfo());

        AppInfo appInfo = new AppInfo();
        reportingData.setAppInfo(appInfo);
        assertEquals(appInfo, reportingData.getAppInfo());
    }

    @Test
    public void testGettersAndSettersUsers() {
        ReportingData reportingData = new ReportingData();
        assertNull(reportingData.getUsers());

        List<User> users = new ArrayList<User>();
        reportingData.setUsers(users);
        assertEquals(users, reportingData.getUsers());
    }

    @Test
    public void testEqualsWithSameObject() {
        ReportingData reportingData = new ReportingData();
        assertTrue(reportingData.equals(reportingData));
    }

    @Test
    public void testEqualsWithEqualObjects() {
        ReportingData data1 = new ReportingData();
        data1.setClients(new ArrayList<Client>());
        data1.setProjects(new ArrayList<Project>());

        ReportingData data2 = new ReportingData();
        data2.setClients(new ArrayList<Client>());
        data2.setProjects(new ArrayList<Project>());

        assertTrue(data1.equals(data2));
        assertTrue(data2.equals(data1));
    }

    @Test
    public void testEqualsWithUnequalObjects() {
        ReportingData data1 = new ReportingData();
        data1.setClients(new ArrayList<Client>());

        ReportingData data2 = new ReportingData();
        List<Client> clients = new ArrayList<Client>();
        clients.add(new Client());
        data2.setClients(clients);

        assertFalse(data1.equals(data2));
    }

    @Test
    public void testEqualsWithNull() {
        ReportingData reportingData = new ReportingData();
        assertFalse(reportingData.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        ReportingData reportingData = new ReportingData();
        assertFalse(reportingData.equals("a string"));
    }

    @Test
    public void testHashCodeConsistentWithEquals() {
        ReportingData data1 = new ReportingData();
        data1.setClients(new ArrayList<Client>());

        ReportingData data2 = new ReportingData();
        data2.setClients(new ArrayList<Client>());

        assertEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    public void testHashCodeDifferentForUnequalObjects() {
        ReportingData data1 = new ReportingData();

        ReportingData data2 = new ReportingData();
        List<Client> clients = new ArrayList<Client>();
        clients.add(new Client());
        data2.setClients(clients);

        assertNotEquals(data1.hashCode(), data2.hashCode());
    }

    @Test
    public void testToStringReturnsNonNull() {
        ReportingData reportingData = new ReportingData();
        assertNotNull(reportingData.toString());
    }
}
