"""Employee repository integration tests."""

import pytest
from sqlalchemy import func, select
from sqlalchemy.orm import Session

from snowman.domain.exception import SnowmanError
from snowman.domain.model.employee import Employee, EmployeeRole
from snowman.domain.repository.impl.employee import SqlAlchemyEmployeeRepository


def _seed_role(db_session: Session) -> EmployeeRole:
    role = EmployeeRole(id=1, role="Engineer")
    db_session.add(role)
    db_session.flush()
    return role


def test_find_employee_returns_none_for_unknown_id(db_session: Session) -> None:
    repository = SqlAlchemyEmployeeRepository(db_session)

    assert repository.find_employee(999) is None


def test_find_employee_returns_persisted_entity(db_session: Session) -> None:
    employee = Employee(firstname="First", surname="Surname")
    db_session.add(employee)
    db_session.flush()
    repository = SqlAlchemyEmployeeRepository(db_session)

    found = repository.find_employee(employee.id)

    assert found is employee


def test_save_employee_inserts_and_resolves_detached_role(
    db_session: Session,
) -> None:
    seeded_role = _seed_role(db_session)
    employee = Employee(
        id=0,
        firstname="First",
        surname="Surname",
        role=EmployeeRole(role="Engineer"),
    )
    repository = SqlAlchemyEmployeeRepository(db_session)

    repository.save_employee(employee)

    assert employee.id != 0
    assert employee.employee_role_id == seeded_role.id
    assert employee.role is seeded_role
    assert db_session.scalar(select(func.count()).select_from(EmployeeRole)) == 1
    assert db_session.scalar(select(func.count()).select_from(Employee)) == 1


def test_save_employee_rejects_unknown_role(db_session: Session) -> None:
    employee = Employee(
        id=0,
        firstname="First",
        surname="Surname",
        role=EmployeeRole(role="Nonexistent"),
    )
    repository = SqlAlchemyEmployeeRepository(db_session)

    with pytest.raises(SnowmanError, match=r"^Unknown employee role: Nonexistent$") as error:
        repository.save_employee(employee)

    assert str(error.value) == "Unknown employee role: Nonexistent"


def test_save_employee_updates_existing_entity(db_session: Session) -> None:
    seeded_role = _seed_role(db_session)
    employee = Employee(firstname="First", surname="Surname", role=seeded_role)
    db_session.add(employee)
    db_session.flush()
    employee_id = employee.id
    updated_employee = Employee(
        id=employee_id,
        firstname="Updated",
        surname="Name",
        role=EmployeeRole(role="Engineer"),
    )
    repository = SqlAlchemyEmployeeRepository(db_session)

    repository.save_employee(updated_employee)

    saved = repository.find_employee(employee_id)
    assert saved is not None
    assert saved.firstname == "Updated"
    assert saved.surname == "Name"
    assert saved.employee_role_id == seeded_role.id
    assert db_session.scalar(select(func.count()).select_from(Employee)) == 1


def test_remove_employee_deletes_existing_entity(db_session: Session) -> None:
    employee = Employee(firstname="First", surname="Surname")
    db_session.add(employee)
    db_session.flush()
    repository = SqlAlchemyEmployeeRepository(db_session)

    repository.remove_employee(employee.id)

    assert repository.find_employee(employee.id) is None


def test_remove_employee_is_noop_for_unknown_id(db_session: Session) -> None:
    repository = SqlAlchemyEmployeeRepository(db_session)

    repository.remove_employee(999)
