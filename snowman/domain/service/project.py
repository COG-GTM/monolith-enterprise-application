"""Project service."""

from __future__ import annotations

from snowman.domain.exception import EntityNotFoundError
from snowman.domain.model.project import Project
from snowman.domain.repository.project import ProjectRepository


class ProjectService:
    """Application operations for projects."""

    def __init__(self, repository: ProjectRepository) -> None:
        self.repository = repository

    def get_project(self, project_id: int) -> Project | None:
        return self.repository.find_project(project_id)

    def create_project(self, project: Project) -> None:
        self.repository.save_project(project)

    def update_project(self, project: Project) -> None:
        if self.get_project(project.id) is None:
            raise EntityNotFoundError(f"Can't update an unknown project {project}")
        self.repository.save_project(project)

    def delete_project(self, project_id: int) -> None:
        if self.get_project(project_id) is None:
            raise EntityNotFoundError(f"Can't remove an unknown project with id: {project_id}")
        self.repository.remove_project(project_id)
