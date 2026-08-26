"""Project repository port."""

from __future__ import annotations

from typing import Protocol

from snowman.domain.model.project import Project


class ProjectRepository(Protocol):
    """Persistence operations required by the project service."""

    def find_project(self, project_id: int) -> Project | None:
        """Find a project by primary key."""

    def save_project(self, project: Project) -> None:
        """Persist a project."""

    def remove_project(self, project_id: int) -> None:
        """Remove a project by primary key."""
