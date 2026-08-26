"""Employee-project association ORM model.

The Java files ``domain/model/EmployeeProject.java``,
``domain/model/EmployeeProjectV2.java``, and
``domain/model/EmployeeProjectId.java`` represent alternate mappings of one table
in different Hibernate dialects; the Python port keeps one composite-key mapping
because Python has no reason to carry two mappings of the same table.
"""

from __future__ import annotations

from datetime import date
from typing import TYPE_CHECKING

from sqlalchemy import Date, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column, relationship

from snowman.db.base import Base

if TYPE_CHECKING:
    from snowman.domain.model.employee import Employee
    from snowman.domain.model.project import Project


class EmployeeProject(Base):
    __tablename__ = "employee_project"
    employee_id: Mapped[int] = mapped_column(ForeignKey("employee.id"), primary_key=True)
    project_id: Mapped[int] = mapped_column(ForeignKey("project.id"), primary_key=True)
    date_started: Mapped[date | None] = mapped_column("date_started", Date)
    date_ended: Mapped[date | None] = mapped_column("date_ended", Date)
    employee: Mapped[Employee] = relationship(back_populates="projects")
    project: Mapped[Project] = relationship(back_populates="employee_projects")
