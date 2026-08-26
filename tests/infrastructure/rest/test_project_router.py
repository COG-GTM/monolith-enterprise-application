"""Project router tests."""

from datetime import date

from snowman.domain.exception import EntityNotFoundError
from snowman.domain.model.project import Project
from snowman.infrastructure.rest.routers.project import get_project_service


class FakeProjectService:
    def __init__(self) -> None:
        self.projects: dict[int, Project] = {
            1: Project(
                id=1,
                project_title="Existing project",
                date_started=date(2024, 1, 1),
            )
        }

    def get_project(self, project_id: int) -> Project | None:
        return self.projects.get(project_id)

    def create_project(self, project: Project) -> None:
        self.projects[project.id] = project

    def update_project(self, project: Project) -> None:
        if project.id not in self.projects:
            raise EntityNotFoundError(f"Can't update an unknown project {project}")
        self.projects[project.id] = project

    def delete_project(self, project_id: int) -> None:
        if project_id not in self.projects:
            raise EntityNotFoundError(f"Can't remove an unknown project with id: {project_id}")
        self.projects.pop(project_id, None)


def test_project_routes(client) -> None:
    service = FakeProjectService()
    client.app.dependency_overrides[get_project_service] = lambda: service
    payload = {
        "projectId": 1,
        "title": "Updated project",
        "dateStarted": "2024-01-01",
        "dateEnded": "2024-02-01",
    }

    response = client.get("/project/1")
    assert response.status_code == 200
    assert set(response.json()) == {"projectId", "title", "dateStarted", "dateEnded"}

    assert client.post("/project/create", json=payload).status_code == 200
    assert client.post("/project/update", json=payload).status_code == 200
    assert client.post("/project/update}", json=payload).status_code == 200
    assert client.delete("/project/1/delete").status_code == 200

    assert client.get("/project/999").status_code == 404
    assert client.post("/project/update", json={**payload, "projectId": 999}).status_code == 404
    assert client.delete("/project/999/delete").status_code == 404

    openapi = client.get("/openapi.json").json()
    assert "/project/update" in openapi["paths"]
    assert "/project/update}" not in openapi["paths"]
