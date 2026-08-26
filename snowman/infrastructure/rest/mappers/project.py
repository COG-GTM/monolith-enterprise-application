"""Project resource mappings."""

from __future__ import annotations

from collections.abc import Iterable
from datetime import date
from typing import cast

from snowman.domain.model.project import Project
from snowman.infrastructure.rest.resources.project import ProjectResource


def to_resource(project: Project) -> ProjectResource:
    """Map a project domain model to its REST resource."""

    return ProjectResource(
        projectId=project.id,
        clientId=project.client_id,
        title=project.project_title,
        dateStarted=project.date_started,
        dateEnded=project.date_ended,
    )


def to_project(resource: ProjectResource) -> Project:
    """Map a project REST resource to its domain model."""

    # The client join is not part of the project payload.
    # clientId is deviation 11, an additive field absent from Java's ProjectResource.
    # The resource permits missing dateStarted; the column does not, matching Java pass-through.
    project = Project(
        id=resource.projectId,
        project_title=resource.title,
        date_started=cast(date, resource.dateStarted),
        date_ended=resource.dateEnded,
    )
    if resource.clientId is not None:
        project.client_id = resource.clientId
    return project


def to_projects(resources: Iterable[ProjectResource]) -> list[Project]:
    """Map project resources to domain models."""

    return [to_project(resource) for resource in resources]


def to_resources(projects: Iterable[Project]) -> list[ProjectResource]:
    """Map projects to REST resources."""

    return [to_resource(project) for project in projects]
