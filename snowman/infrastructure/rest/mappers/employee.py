"""Mappings between Employee domain objects and REST resources."""

from snowman.domain.model.employee import Employee, EmployeeRole
from snowman.infrastructure.rest.resources.employee import EmployeeResource


def to_resource(employee: Employee) -> EmployeeResource:
    """Map an Employee domain object to its REST representation."""

    return EmployeeResource(
        employeeId=employee.id,
        firstName=employee.firstname,
        secondName=employee.surname,
        role=employee.role.role if employee.role is not None else None,
    )


def to_employee(resource: EmployeeResource) -> Employee:
    """Map an Employee REST resource to a detached domain object."""

    employee = Employee(
        id=resource.employeeId,
        firstname=resource.firstName,
        surname=resource.secondName,
    )
    # Java's mapper only attaches a supplied role, and the nullable FK permits it.
    if resource.role is not None:
        employee.role = EmployeeRole(role=resource.role)
    return employee
