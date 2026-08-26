"""SQLAlchemy engine and transaction dependency."""

from collections.abc import Iterator

from sqlalchemy import create_engine
from sqlalchemy.engine import Engine
from sqlalchemy.orm import Session, sessionmaker

from snowman.config import get_settings


def _build_engine() -> Engine:
    settings = get_settings()
    if settings.database_url.startswith("sqlite"):
        return create_engine(
            settings.database_url,
            echo=settings.echo_sql,
            connect_args={"check_same_thread": False},
        )
    return create_engine(settings.database_url, echo=settings.echo_sql)


engine = _build_engine()
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


def get_db() -> Iterator[Session]:
    """Yield a transaction-scoped session and commit successful requests."""

    database = SessionLocal()
    try:
        yield database
        database.commit()
    except Exception:
        database.rollback()
        raise
    finally:
        database.close()
