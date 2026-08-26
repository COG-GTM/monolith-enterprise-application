"""Project router tests."""

from datetime import date

from snowman.domain.model.client import Client
from snowman.domain.model.project import Project


def seed_project(db_session, project_id: int = 1) -> Project:
    project = Project(
        id=project_id,
        project_title="Existing project",
        date_started=date(2024, 1, 1),
        client=Client(id=100 + project_id, client_name="Router client"),
    )
    db_session.add(project)
    db_session.flush()
    return project


def project_payload(project_id: int, title: str = "Updated project") -> dict[str, object]:
    return {
        "projectId": project_id,
        "title": title,
        "dateStarted": "2024-01-01",
        "dateEnded": "2024-02-01",
    }


def test_get_project_returns_resource_values(client, db_session) -> None:
    seed_project(db_session)

    response = client.get("/project/1")

    assert response.status_code == 200
    assert response.json() == {
        "projectId": 1,
        "title": "Existing project",
        "dateStarted": "2024-01-01",
        "dateEnded": None,
    }


def test_create_project_returns_empty_body_and_persists_changes(client, db_session) -> None:
    seed_project(db_session)

    response = client.post("/project/create", json=project_payload(1, "Created project"))

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").json()["title"] == "Created project"


def test_update_project_returns_empty_body_and_persists_changes(client, db_session) -> None:
    seed_project(db_session)

    response = client.post("/project/update", json=project_payload(1, "Updated project"))

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").json()["title"] == "Updated project"


def test_legacy_update_project_returns_empty_body(client, db_session) -> None:
    seed_project(db_session)

    response = client.post("/project/update}", json=project_payload(1, "Legacy project"))

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").json()["title"] == "Legacy project"


def test_delete_project_returns_empty_body_and_removes_project(client, db_session) -> None:
    seed_project(db_session)

    response = client.delete("/project/1/delete")

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").status_code == 404


def test_unknown_project_routes_return_not_found(client, db_session) -> None:
    seed_project(db_session)

    assert client.get("/project/999").status_code == 404
    assert client.post("/project/update", json=project_payload(999)).status_code == 404
    assert client.delete("/project/999/delete").status_code == 404


def test_project_openapi_exposes_only_canonical_update_route(client) -> None:
    paths = client.get("/openapi.json").json()["paths"]

    assert "/project/update" in paths
    assert "/project/update}" not in paths
