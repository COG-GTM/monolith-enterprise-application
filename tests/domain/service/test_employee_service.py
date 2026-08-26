"""Employee service tests."""

import pytest

from snowman.domain.exception import EntityNotFoundError
from snowman.domain.model.employee import Employee
from snowman.domain.service.employee import EmployeeService


class StubEmployeeRepository:
    def __init__(self, employee: Employee | None = None) -> None:
        self.employee = employee
        self.saved: list[Employee] = []
        self.removed: list[int] = []

    def find_employee(self, employee_id: int) -> Employee | None:
        if self.employee is not None and self.employee.id == employee_id:
            return self.employee
        return None

    def save_employee(self, employee: Employee) -> None:
        self.saved.append(employee)

    def remove_employee(self, employee_id: int) -> None:
        self.removed.append(employee_id)


def test_get_employee_delegates_to_repository() -> None:
    employee = Employee(id=3, firstname="First", surname="Surname")
    repository = StubEmployeeRepository(employee)

    assert EmployeeService(repository).get_employee(3) is employee


def test_create_employee_saves_without_existence_check() -> None:
    employee = Employee(id=3, firstname="First", surname="Surname")
    repository = StubEmployeeRepository()

    EmployeeService(repository).create_employee(employee)

    assert repository.saved == [employee]


def test_update_employee_saves_existing_employee() -> None:
    employee = Employee(id=3, firstname="First", surname="Surname")
    repository = StubEmployeeRepository(employee)

    EmployeeService(repository).update_employee(employee)

    assert repository.saved == [employee]


def test_update_employee_raises_for_unknown_employee() -> None:
    employee = Employee(id=3, firstname="First", surname="Surname")
    repository = StubEmployeeRepository()

    with pytest.raises(
        EntityNotFoundError,
        match=r"There is no existing employee with id: 3",
    ):
        EmployeeService(repository).update_employee(employee)


def test_delete_employee_removes_existing_employee() -> None:
    employee = Employee(id=3, firstname="First", surname="Surname")
    repository = StubEmployeeRepository(employee)

    EmployeeService(repository).delete_employee(3)

    assert repository.removed == [3]


def test_delete_employee_raises_for_unknown_employee() -> None:
    repository = StubEmployeeRepository()

    with pytest.raises(
        EntityNotFoundError,
        match=r"There is no existing employee with id: 3",
    ):
        EmployeeService(repository).delete_employee(3)
