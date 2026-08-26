from snowman.domain.model import Employee
from snowman.infrastructure.messaging.converter.project import to_project_dtos
from snowman.infrastructure.messaging.dto import EmployeeDTO


def to_employee_dto(employee: Employee) -> EmployeeDTO:
    role = employee.role.role if employee.role is not None else None
    projects = (employee_project.project for employee_project in employee.projects)
    return EmployeeDTO(
        id=employee.id,
        firstName=employee.firstname,
        surname=employee.surname,
        role=role,
        projectDTOList=to_project_dtos(projects),
    )
