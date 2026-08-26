from collections.abc import Iterable

from snowman.domain.model import Project
from snowman.infrastructure.messaging.dto import ProjectDTO


def to_project_dto(project: Project) -> ProjectDTO:
    return ProjectDTO(
        projectId=project.id,
        projectTitle=project.project_title,
        dateStarted=project.date_started,
        dateEnded=project.date_ended,
    )


def to_project_dtos(projects: Iterable[Project]) -> list[ProjectDTO]:
    return [to_project_dto(project) for project in projects]
