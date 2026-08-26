"""Application metadata ORM model."""

from __future__ import annotations

from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from snowman.db.base import Base


class AppInfo(Base):
    __tablename__ = "app_info"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)
    version: Mapped[str | None] = mapped_column(String(20))
