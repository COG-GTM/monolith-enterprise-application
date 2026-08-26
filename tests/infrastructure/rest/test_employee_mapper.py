"""Employee mapper tests."""

from snowman.domain.model.employee import Employee, EmployeeRole
from snowman.infrastructure.rest.mappers.employee import to_employee, to_resource
from snowman.infrastructure.rest.resources.employee import EmployeeResource


def test_to_resource_maps_employee_fields() -> None:
    employee = Employee(
        id=1,
        firstname="First",
        surname="Surname",
        role=EmployeeRole(id=7, role="Engineer"),
    )

    assert to_resource(employee).model_dump() == {
        "employeeId": 1,
        "firstName": "First",
        "secondName": "Surname",
        "role": "Engineer",
    }


def test_to_resource_maps_missing_role_to_none() -> None:
    employee = Employee(id=1, firstname="First", surname="Surname")

    assert to_resource(employee).role is None


def test_to_employee_maps_role_to_new_detached_role() -> None:
    resource = EmployeeResource(
        employeeId=1,
        firstName="First",
        secondName="Surname",
        role="Engineer",
    )

    employee = to_employee(resource)

    assert employee.id == 1
    assert employee.firstname == "First"
    assert employee.surname == "Surname"
    assert employee.role is not None
    assert employee.role.id is None
    assert employee.role.role == "Engineer"


def test_to_employee_leaves_missing_role_none() -> None:
    resource = EmployeeResource(
        employeeId=1,
        firstName="First",
        secondName="Surname",
    )

    assert to_employee(resource).role is None
