"""Employee-project ORM relationship tests."""

from datetime import date

from snowman.domain.model.client import Client
from snowman.domain.model.employee import Employee
from snowman.domain.model.employee_project import EmployeeProject
from snowman.domain.model.project import Project


def test_employee_project_relationships_round_trip(db_session) -> None:
    employee = Employee(firstname="Ada", surname="Lovelace")
    client = Client(id=1, client_name="Model client")
    project = Project(
        project_title="Model project",
        date_started=date(2024, 1, 1),
        client=client,
    )
    employee_project = EmployeeProject(
        employee=employee,
        project=project,
        date_started=date(2024, 1, 2),
    )
    db_session.add_all([employee, client, project, employee_project])
    db_session.flush()

    assert employee.projects[0].project is project
    assert project.employee_projects[0].employee is employee
