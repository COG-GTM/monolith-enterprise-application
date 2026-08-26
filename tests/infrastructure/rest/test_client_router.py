"""Mirrors `ClientRestEndpoint` behavior over the FastAPI router."""

from collections.abc import Iterator
from datetime import date

import pytest
from fastapi.testclient import TestClient
from sqlalchemy.orm import Session

from snowman.db.session import get_db
from snowman.domain.model.client import Client
from snowman.domain.model.project import Project
from snowman.domain.service.client import ClientSystemGateway
from snowman.infrastructure.rest.routers import client as client_router
from snowman.main import create_app
from tests.client_doubles import RecordingClientSystemGateway


@pytest.fixture
def gateway() -> RecordingClientSystemGateway:
    return RecordingClientSystemGateway(responses=[(200, "{}")])


@pytest.fixture
def api(db_session: Session, gateway: RecordingClientSystemGateway) -> Iterator[TestClient]:
    app = create_app()

    def _get_db() -> Iterator[Session]:
        yield db_session

    def _get_gateway() -> ClientSystemGateway:
        return gateway

    app.dependency_overrides[get_db] = _get_db
    app.dependency_overrides[client_router.get_client_system] = _get_gateway
    with TestClient(app) as test_client:
        yield test_client
    app.dependency_overrides.clear()


def _seed(session: Session, client_id: int = 1, with_project: bool = True) -> None:
    client = Client()
    client.id = client_id
    client.client_name = "Acme"
    session.add(client)
    session.flush()
    if with_project:
        project = Project()
        project.project_title = "Snowman"
        project.date_started = date(2018, 1, 1)
        project.client_id = client_id
        session.add(project)
    session.flush()


def test_get_client_returns_the_resource_json(api: TestClient, db_session: Session) -> None:
    _seed(db_session)

    response = api.get("/client/1")

    assert response.status_code == 200
    body = response.json()
    assert body["clientId"] == 1
    assert body["clientName"] == "Acme"
    assert [p["title"] for p in body["projects"]] == ["Snowman"]


def test_get_unknown_client_returns_404(api: TestClient) -> None:
    assert api.get("/client/999").status_code == 404


def test_create_client_returns_200_with_empty_body(api: TestClient, db_session: Session) -> None:
    response = api.post("/client/new", json={"clientId": 5, "clientName": "Globex"})

    assert response.status_code == 200
    assert response.json() is None
    assert db_session.get(Client, 5) is not None


def test_create_existing_client_returns_500(api: TestClient, db_session: Session) -> None:
    _seed(db_session, client_id=6)

    response = api.post("/client/new", json={"clientId": 6, "clientName": "Acme"})

    assert response.status_code == 500
    assert response.json() == {"detail": "Client already exists"}


def test_update_client_returns_200(api: TestClient, db_session: Session) -> None:
    _seed(db_session, client_id=7)

    response = api.post("/client/update", json={"clientId": 7, "clientName": "Renamed"})

    assert response.status_code == 200
    assert response.json() is None


def test_update_unknown_client_returns_500(api: TestClient) -> None:
    response = api.post("/client/update", json={"clientId": 8, "clientName": "Nope"})

    assert response.status_code == 500
    assert response.json() == {"detail": "Client doesn't exists"}


def test_delete_client_returns_200(api: TestClient, db_session: Session) -> None:
    _seed(db_session, client_id=9, with_project=False)

    response = api.delete("/client/9")

    assert response.status_code == 200
    assert response.json() is None


def test_delete_unknown_client_returns_500(api: TestClient) -> None:
    response = api.delete("/client/404")

    assert response.status_code == 500
    assert response.json() == {"detail": "Client doesn't exists"}
