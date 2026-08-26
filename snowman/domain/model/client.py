"""Client ORM model."""

from __future__ import annotations

from typing import TYPE_CHECKING

from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from snowman.db.base import Base

if TYPE_CHECKING:
    from snowman.domain.model.project import Project


class Client(Base):
    __tablename__ = "client"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)
    client_name: Mapped[str] = mapped_column("client_name", String(30), nullable=False)
    projects: Mapped[list["Project"]] = relationship(back_populates="client", lazy="select")
