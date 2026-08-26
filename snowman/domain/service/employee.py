"""Employee domain service."""

from snowman.domain.exception import EntityNotFoundError
from snowman.domain.model.employee import Employee
from snowman.domain.repository.employee import EmployeeRepository


class EmployeeService:
    """Apply Employee service business rules."""

    def __init__(self, repository: EmployeeRepository) -> None:
        self._repository = repository

    def get_employee(self, employee_id: int) -> Employee | None:
        return self._repository.find_employee(employee_id)

    def create_employee(self, employee: Employee) -> None:
        self._repository.save_employee(employee)

    def update_employee(self, employee: Employee) -> None:
        if not self.get_employee(employee.id):
            raise EntityNotFoundError(
                f"There is no existing employee with id: {employee.id}"
            )
        self._repository.save_employee(employee)

    def delete_employee(self, employee_id: int) -> None:
        if not self.get_employee(employee_id):
            raise EntityNotFoundError(
                f"There is no existing employee with id: {employee_id}"
            )
        self._repository.remove_employee(employee_id)
