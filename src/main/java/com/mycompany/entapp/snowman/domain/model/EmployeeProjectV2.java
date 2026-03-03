/*
 * |-------------------------------------------------
 * | Copyright © 2017 Colin But. All rights reserved.
 * |-------------------------------------------------
 */
package com.mycompany.entapp.snowman.domain.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "employee_project")
public class EmployeeProjectV2 implements Serializable {

    @EmbeddedId
    private EmployeeProjectId primaryKey = new EmployeeProjectId();

    // Java 8: Migrated from java.util.Date to java.time.LocalDate, removed @Temporal
    @Column(name = "date_started")
    private LocalDate dateStarted;

    @Column(name = "date_ended")
    private LocalDate dateEnded;

    public LocalDate getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(LocalDate dateStarted) {
        this.dateStarted = dateStarted;
    }

    public LocalDate getDateEnded() {
        return dateEnded;
    }

    public void setDateEnded(LocalDate dateEnded) {
        this.dateEnded = dateEnded;
    }

    public EmployeeProjectId getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(EmployeeProjectId primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Transient
    public Employee getEmployee() {
        return primaryKey.getEmployee();
    }

    public void setEmployee(Employee employee) {
        primaryKey.setEmployee(employee);
    }

    public Project getProject() {
        return primaryKey.getProject();
    }

    public void setProject(Project project) {
        primaryKey.setProject(project);
    }


}
