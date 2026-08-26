from collections.abc import Iterable

from snowman.infrastructure.messaging.converter.protocols import ProjectLike
from snowman.infrastructure.messaging.dto import ProjectDTO


def to_project_dto(project: ProjectLike) -> ProjectDTO:
    return ProjectDTO(
        projectId=project.id,
        projectTitle=project.project_title,
        dateStarted=project.date_started,
        dateEnded=project.date_ended,
    )


def to_project_dtos(projects: Iterable[ProjectLike]) -> list[ProjectDTO]:
    return [to_project_dto(project) for project in projects]
