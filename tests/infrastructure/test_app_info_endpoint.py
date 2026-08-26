from sqlalchemy.orm import Session

from snowman.domain.model.app_info import AppInfo
from snowman.domain.repository.impl.app_info import ApplicationInfoRepositoryImpl
from snowman.infrastructure.db.app_info_dao import load_application_infos


def _initialize_repository(client, db_session: Session) -> ApplicationInfoRepositoryImpl:
    repository = client.app.state.application_info_repository
    assert isinstance(repository, ApplicationInfoRepositoryImpl)
    repository._load_app_infos = lambda: load_application_infos(db_session)
    repository._initialized = False
    repository.initialize()
    return repository


def test_app_info_endpoint_returns_single_app_info(
    client,
    db_session: Session,
) -> None:
    db_session.add(AppInfo(id=1, version="1.0.0"))
    db_session.flush()
    _initialize_repository(client, db_session)

    response = client.get("/app/info")

    assert response.status_code == 200
    assert response.json() == {"id": 1, "version": "1.0.0"}


def test_app_info_endpoint_returns_business_error_for_empty_table(client) -> None:
    response = client.get("/app/info")

    assert response.status_code == 500
    assert response.json() == {"detail": "AppInfo is null or empty"}


def test_app_info_endpoint_returns_business_error_for_multiple_rows(
    client,
    db_session: Session,
) -> None:
    db_session.add_all(
        [
            AppInfo(id=1, version="1.0.0"),
            AppInfo(id=2, version="2.0.0"),
        ]
    )
    db_session.flush()
    _initialize_repository(client, db_session)

    response = client.get("/app/info")

    assert response.status_code == 500
    assert response.json() == {"detail": "There are more than one entry in AppInfo"}


def test_app_info_is_loaded_once_across_requests(
    client,
    db_session: Session,
) -> None:
    db_session.add(AppInfo(id=1, version="1.0.0"))
    db_session.flush()
    repository = _initialize_repository(client, db_session)
    original_loader = repository._load_app_infos
    calls = 0

    def load_once() -> list[AppInfo]:
        nonlocal calls
        calls += 1
        return original_loader()

    repository._load_app_infos = load_once
    repository._app_info_map = {}
    repository._initialized = False
    repository.initialize()

    assert client.get("/app/info").status_code == 200
    assert client.get("/app/info").status_code == 200
    assert calls == 1
