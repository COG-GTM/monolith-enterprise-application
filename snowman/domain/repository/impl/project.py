"""SQLAlchemy project repository."""

from __future__ import annotations

from sqlalchemy.orm import Session

from snowman.domain.model.project import Project


class SqlAlchemyProjectRepository:
    """SQLAlchemy adapter for the project repository port."""

    def __init__(self, session: Session) -> None:
        self.session = session

    def find_project(self, project_id: int) -> Project | None:
        return self.session.get(Project, project_id)

    def save_project(self, project: Project) -> None:
        self.session.merge(project)
        self.session.flush()

    def remove_project(self, project_id: int) -> None:
        entity = self.find_project(project_id)
        if entity is not None:
            self.session.delete(entity)
