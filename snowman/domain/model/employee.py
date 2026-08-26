"""Employee and employee-role ORM models."""

from __future__ import annotations

from typing import TYPE_CHECKING

from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from snowman.db.base import Base

if TYPE_CHECKING:
    from snowman.domain.model.employee_project import EmployeeProject


class EmployeeRole(Base):
    __tablename__ = "employee_role"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)
    role: Mapped[str] = mapped_column(String(30), nullable=False)


class Employee(Base):
    __tablename__ = "employee"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    firstname: Mapped[str] = mapped_column(String(20), nullable=False)
    surname: Mapped[str] = mapped_column(String(20), nullable=False)
    employee_role_id: Mapped[int | None] = mapped_column(ForeignKey("employee_role.id", ondelete="CASCADE"))
    role: Mapped[EmployeeRole | None] = relationship(lazy="joined")
    projects: Mapped[list["EmployeeProject"]] = relationship(back_populates="employee", lazy="select")
