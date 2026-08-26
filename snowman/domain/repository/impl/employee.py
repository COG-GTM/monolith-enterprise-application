"""SQLAlchemy employee repository."""

from sqlalchemy import select
from sqlalchemy.orm import Session

from snowman.domain.exception import SnowmanError
from snowman.domain.model.employee import Employee, EmployeeRole


class SqlAlchemyEmployeeRepository:
    """Persist Employee entities with SQLAlchemy."""

    def __init__(self, session: Session) -> None:
        self._session = session

    def find_employee(self, employee_id: int) -> Employee | None:
        """Find an employee by primary key."""

        return self._session.get(Employee, employee_id)

    def save_employee(self, employee: Employee) -> None:
        """Save an employee, treating id zero as an unset transient id.

        Hibernate assigns an autoincremented id when its transient entity has the
        default Java ``int`` id of zero, so zero is converted to ``None`` before
        SQLAlchemy merges the entity.
        """

        if employee.role is not None and employee.role.id is None:
            role_name = employee.role.role
            persistent_role = self._session.scalar(
                select(EmployeeRole).where(EmployeeRole.role == role_name)
            )
            if persistent_role is None:
                raise SnowmanError(f"Unknown employee role: {role_name}")
            employee.role = persistent_role
            employee.employee_role_id = persistent_role.id

        if employee.id == 0:
            employee.id = None  # type: ignore[assignment]
        merged = self._session.merge(employee)
        self._session.flush()
        employee.id = merged.id
        employee.employee_role_id = merged.employee_role_id

    def remove_employee(self, employee_id: int) -> None:
        """Delete an employee if it still exists."""

        employee = self.find_employee(employee_id)
        if employee is not None:
            self._session.delete(employee)
            self._session.flush()
