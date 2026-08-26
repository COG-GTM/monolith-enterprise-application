"""Port of `ProjectResourceMapper.java` (WS3-owned; minimal WS2 stand-in)."""

from collections.abc import Iterable

from snowman.domain.model.project import Project
from snowman.infrastructure.rest.resources.project import ProjectResource


def to_resource(project: Project) -> ProjectResource:
    return ProjectResource(
        projectId=project.id,
        title=project.project_title,
        dateStarted=project.date_started,
        dateEnded=project.date_ended,
    )


def to_project(resource: ProjectResource) -> Project:
    # Java's mapToProject deliberately leaves the client unset (`//project.setClient`): the
    # employee/client join is not part of the payload.
    project = Project()
    project.id = resource.projectId
    project.project_title = resource.title
    if resource.dateStarted is not None:
        project.date_started = resource.dateStarted
    project.date_ended = resource.dateEnded
    return project


def to_resources(projects: Iterable[Project]) -> list[ProjectResource]:
    return [to_resource(project) for project in projects]


def to_projects(resources: Iterable[ProjectResource]) -> list[Project]:
    return [to_project(resource) for resource in resources]
