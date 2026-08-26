"""Project mapper tests."""

from datetime import date

from snowman.infrastructure.rest.mappers.project import (
    to_project,
    to_projects,
    to_resource,
    to_resources,
)
from snowman.infrastructure.rest.resources.project import ProjectResource


def resource(project_id: int = 1) -> ProjectResource:
    return ProjectResource(
        projectId=project_id,
        title="Mapped project",
        dateStarted=date(2024, 1, 1),
        dateEnded=date(2024, 2, 1),
    )


def test_to_project_and_to_resource_round_trip() -> None:
    mapped = to_project(resource())
    result = to_resource(mapped)

    assert result.model_dump() == resource().model_dump()
    assert mapped.client is None


def test_to_projects_and_to_resources() -> None:
    resources = [resource(1), resource(2)]
    projects = to_projects(resources)

    assert to_resources(projects) == resources
