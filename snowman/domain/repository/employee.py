"""Employee repository port."""

from typing import Protocol

from snowman.domain.model.employee import Employee


class EmployeeRepository(Protocol):
    def find_employee(self, employee_id: int) -> Employee | None:
        ...

    def save_employee(self, employee: Employee) -> None:
        ...

    def remove_employee(self, employee_id: int) -> None:
        ...
