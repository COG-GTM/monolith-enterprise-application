"""Shared pytest fixtures."""

from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.engine import Engine
from sqlalchemy.orm import Session, sessionmaker
from sqlalchemy.pool import StaticPool

from snowman.config import get_settings
from snowman.db.base import Base
from snowman.db.session import get_db
from snowman.infrastructure.cache.deps import get_client_cache
from snowman.infrastructure.messaging.in_memory import InMemoryMessageBroker


@pytest.fixture(autouse=True)
def clean_client_cache() -> Iterator[None]:
    get_client_cache().clear()
    get_client_cache.cache_clear()
    try:
        yield
    finally:
        get_client_cache().clear()
        get_client_cache.cache_clear()


@pytest.fixture(scope="session")
def db_engine() -> Iterator[Engine]:
    engine = create_engine(
        "sqlite://",
        connect_args={"check_same_thread": False},
        poolclass=StaticPool,
    )
    Base.metadata.create_all(engine)
    yield engine
    Base.metadata.drop_all(engine)
    engine.dispose()


@pytest.fixture
def db_session(db_engine: Engine) -> Iterator[Session]:
    connection = db_engine.connect()
    transaction = connection.begin()
    session_factory = sessionmaker(bind=connection, autoflush=False, expire_on_commit=False)
    session = session_factory()
    try:
        yield session
    finally:
        session.close()
        transaction.rollback()
        connection.close()


@pytest.fixture
def client(
    db_session: Session,
    monkeypatch: pytest.MonkeyPatch,
) -> Iterator[TestClient]:
    monkeypatch.setenv("SNOWMAN_SCHEDULER_ENABLED", "false")
    get_settings.cache_clear()
    from snowman.main import create_app

    application = create_app()

    def override_get_db() -> Iterator[Session]:
        yield db_session

    application.dependency_overrides[get_db] = override_get_db
    try:
        with TestClient(application) as test_client:
            yield test_client
    finally:
        get_settings.cache_clear()


@pytest.fixture
def broker() -> InMemoryMessageBroker:
    return InMemoryMessageBroker()
