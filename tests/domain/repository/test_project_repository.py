"""Project repository tests."""

from datetime import date

from snowman.domain.model.client import Client
from snowman.domain.model.project import Project
from snowman.domain.repository.impl.project import SqlAlchemyProjectRepository


def make_project() -> Project:
    return Project(
        id=1,
        project_title="Repository project",
        date_started=date(2024, 1, 1),
        client=Client(id=1, client_name="Repository client"),
    )


def test_find_save_and_remove_project(db_session) -> None:
    repository = SqlAlchemyProjectRepository(db_session)
    project = make_project()

    repository.save_project(project)
    persisted = repository.find_project(project.id)

    assert persisted is not None
    assert persisted.project_title == "Repository project"
    repository.remove_project(persisted.id)
    db_session.flush()
    assert repository.find_project(persisted.id) is None


def test_find_and_remove_unknown_project(db_session) -> None:
    repository = SqlAlchemyProjectRepository(db_session)

    assert repository.find_project(9999) is None
    repository.remove_project(9999)
