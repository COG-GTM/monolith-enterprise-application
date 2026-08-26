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


def project_payload(
    project_id: int,
    title: str = "Updated project",
    client_id: int | None = None,
) -> dict[str, object]:
    payload: dict[str, object] = {
        "projectId": project_id,
        "title": title,
        "dateStarted": "2024-01-01",
        "dateEnded": "2024-02-01",
    }
    if client_id is not None:
        payload["clientId"] = client_id
    return payload


def test_get_project_returns_resource_values(client, db_session) -> None:
    seed_project(db_session)

    response = client.get("/project/1")

    assert response.status_code == 200
    assert response.json() == {
        "projectId": 1,
        "clientId": 101,
        "title": "Existing project",
        "dateStarted": "2024-01-01",
        "dateEnded": None,
    }


def test_create_project_returns_empty_body_and_persists_changes(client, db_session) -> None:
    seed_project(db_session)

    response = client.post(
        "/project/create",
        json=project_payload(2, "Created project", client_id=101),
    )

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/2").json() == {
        "projectId": 2,
        "clientId": 101,
        "title": "Created project",
        "dateStarted": "2024-01-01",
        "dateEnded": "2024-02-01",
    }


def test_create_projects_without_ids_assigns_distinct_nonzero_ids(client, db_session) -> None:
    seed_project(db_session)

    first_response = client.post(
        "/project/create",
        json={
            "title": "Generated project one",
            "dateStarted": "2024-01-01",
            "clientId": 101,
        },
    )
    second_response = client.post(
        "/project/create",
        json={
            "title": "Generated project two",
            "dateStarted": "2024-01-02",
            "clientId": 101,
        },
    )

    assert first_response.status_code == 200
    assert second_response.status_code == 200
    first = db_session.query(Project).filter_by(project_title="Generated project one").one()
    second = db_session.query(Project).filter_by(project_title="Generated project two").one()
    assert first.id > 0
    assert second.id > 0
    assert first.id != second.id
    assert client.get(f"/project/{first.id}").json()["title"] == "Generated project one"
    assert client.get(f"/project/{second.id}").json()["title"] == "Generated project two"


def test_update_project_returns_empty_body_and_persists_changes(client, db_session) -> None:
    seed_project(db_session)

    response = client.post(
        "/project/update",
        json=project_payload(1, "Updated project", client_id=101),
    )

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").json()["title"] == "Updated project"
    assert client.get("/project/1").json()["clientId"] == 101


def test_update_project_without_client_id_preserves_existing_client(
    client,
    db_session,
) -> None:
    seed_project(db_session)

    response = client.post("/project/update", json=project_payload(1, "Updated without client"))

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").json()["title"] == "Updated without client"
    assert client.get("/project/1").json()["clientId"] == 101


def test_legacy_update_project_returns_empty_body(client, db_session) -> None:
    seed_project(db_session)

    response = client.post(
        "/project/update}",
        json=project_payload(1, "Legacy project", client_id=101),
    )

    assert response.status_code == 200
    assert response.text == ""
    assert client.get("/project/1").json()["title"] == "Legacy project"
    assert client.get("/project/1").json()["clientId"] == 101


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
