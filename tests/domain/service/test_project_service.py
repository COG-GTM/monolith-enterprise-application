"""Project service tests."""

from dataclasses import dataclass, field
from datetime import date

import pytest

from snowman.domain.exception import EntityNotFoundError
from snowman.domain.model.project import Project
from snowman.domain.service.project import ProjectService


@dataclass
class FakeProjectRepository:
    projects: dict[int, Project] = field(default_factory=dict)
    saved: list[Project] = field(default_factory=list)
    removed: list[int] = field(default_factory=list)

    def find_project(self, project_id: int) -> Project | None:
        return self.projects.get(project_id)

    def save_project(self, project: Project) -> None:
        self.saved.append(project)
        self.projects[project.id] = project

    def remove_project(self, project_id: int) -> None:
        self.removed.append(project_id)


def project(project_id: int = 1) -> Project:
    return Project(
        id=project_id,
        project_title="Project",
        date_started=date(2024, 1, 1),
    )


def test_get_project_delegates_to_repository() -> None:
    expected = project()
    repository = FakeProjectRepository(projects={expected.id: expected})

    assert ProjectService(repository).get_project(expected.id) is expected


def test_create_project_saves_unconditionally() -> None:
    expected = project()
    repository = FakeProjectRepository()

    ProjectService(repository).create_project(expected)

    assert repository.saved == [expected]


def test_update_project_saves_existing_project() -> None:
    expected = project()
    repository = FakeProjectRepository(projects={expected.id: expected})

    ProjectService(repository).update_project(expected)

    assert repository.saved == [expected]


def test_update_unknown_project_raises_without_saving() -> None:
    expected = project()
    repository = FakeProjectRepository()

    with pytest.raises(
        EntityNotFoundError,
        match=r"^Can't update an unknown project .*$",
    ) as error:
        ProjectService(repository).update_project(expected)

    assert str(error.value) == f"Can't update an unknown project {expected}"
    assert repository.saved == []


def test_delete_project_removes_existing_project() -> None:
    expected = project()
    repository = FakeProjectRepository(projects={expected.id: expected})

    ProjectService(repository).delete_project(expected.id)

    assert repository.removed == [expected.id]


def test_delete_unknown_project_raises_without_removing() -> None:
    repository = FakeProjectRepository()

    with pytest.raises(EntityNotFoundError) as error:
        ProjectService(repository).delete_project(1)

    assert str(error.value) == "Can't remove an unknown project with id: 1"
    assert repository.removed == []
