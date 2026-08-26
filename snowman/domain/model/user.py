"""User ORM model."""

from __future__ import annotations

from sqlalchemy import String
from sqlalchemy.orm import Mapped, mapped_column

from snowman.db.base import Base


class User(Base):
    __tablename__ = "user"
    user_id: Mapped[int] = mapped_column("id", primary_key=True, autoincrement=False)
    username: Mapped[str | None] = mapped_column(String(20))
    password: Mapped[str | None] = mapped_column(String(20))
    email: Mapped[str | None] = mapped_column(String(20))
    firstname: Mapped[str | None] = mapped_column(String(20))
    lastname: Mapped[str | None] = mapped_column("secondname", String(20))
