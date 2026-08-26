"""Project ORM model."""

from __future__ import annotations

from datetime import date
from typing import TYPE_CHECKING

from sqlalchemy import Date, ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from snowman.db.base import Base

if TYPE_CHECKING:
    from snowman.domain.model.client import Client
    from snowman.domain.model.employee_project import EmployeeProject


class Project(Base):
    __tablename__ = "project"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    project_title: Mapped[str] = mapped_column("project_title", String(20), nullable=False)
    date_started: Mapped[date] = mapped_column("date_started", Date, nullable=False)
    date_ended: Mapped[date | None] = mapped_column("date_ended", Date)
    client_id: Mapped[int] = mapped_column(
        ForeignKey("client.id", ondelete="CASCADE"), nullable=False
    )
    client: Mapped[Client] = relationship(back_populates="projects")
    employee_projects: Mapped[list[EmployeeProject]] = relationship(
        back_populates="project", lazy="select"
    )
