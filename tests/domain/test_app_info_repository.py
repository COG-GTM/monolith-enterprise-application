from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from snowman.db.base import Base
from snowman.domain.model.app_info import AppInfo
from snowman.domain.repository.impl.app_info import ApplicationInfoRepositoryImpl
from snowman.infrastructure.db.app_info_dao import load_application_infos


def test_initialize_populates_map_once_and_is_idempotent() -> None:
    calls = 0

    def load() -> list[AppInfo]:
        nonlocal calls
        calls += 1
        return [AppInfo(id=1, version="1.0.0")]

    repository = ApplicationInfoRepositoryImpl(load)
    repository.initialize()
    repository.initialize()

    assert calls == 1
    app_info_map = repository.get_app_info_map()
    assert list(app_info_map) == [1]
    assert app_info_map[1].version == "1.0.0"


def test_missing_table_returns_empty_map_without_raising() -> None:
    engine = create_engine("sqlite://")
    with Session(engine) as session:
        repository = ApplicationInfoRepositoryImpl(lambda: load_application_infos(session))
        repository.initialize()

    assert repository.get_app_info_map() == {}


def test_app_info_table_can_be_created_for_model_metadata() -> None:
    engine = create_engine("sqlite://")
    Base.metadata.create_all(engine)

    with Session(engine) as session:
        session.add(AppInfo(id=1, version="1.0.0"))
        session.commit()
        loaded = load_application_infos(session)

    assert [(item.id, item.version) for item in loaded] == [(1, "1.0.0")]
