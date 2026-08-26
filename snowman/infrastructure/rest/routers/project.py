"""Project REST routes."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import Response
from sqlalchemy.orm import Session

from snowman.db.session import get_db
from snowman.domain.repository.impl.project import SqlAlchemyProjectRepository
from snowman.domain.service.project import ProjectService
from snowman.infrastructure.rest.mappers.project import to_project, to_resource
from snowman.infrastructure.rest.resources.project import ProjectResource

router = APIRouter(prefix="/project", tags=["project"])


def get_project_service(session: Session = Depends(get_db)) -> ProjectService:
    """Build the project service for a request."""

    return ProjectService(SqlAlchemyProjectRepository(session))


@router.post("/create", response_class=Response, status_code=200)
def create_project(
    resource: ProjectResource,
    service: ProjectService = Depends(get_project_service),
) -> Response:
    service.create_project(to_project(resource))
    return Response(status_code=200)


def _update_project(
    resource: ProjectResource,
    service: ProjectService = Depends(get_project_service),
) -> Response:
    service.update_project(to_project(resource))
    return Response(status_code=200)


@router.post("/update", response_class=Response, status_code=200)
def update_project(
    resource: ProjectResource,
    service: ProjectService = Depends(get_project_service),
) -> Response:
    return _update_project(resource, service)


@router.post("/update}", include_in_schema=False, response_class=Response, status_code=200)
def update_project_legacy(
    resource: ProjectResource,
    service: ProjectService = Depends(get_project_service),
) -> Response:
    return _update_project(resource, service)


@router.get("/{projectId}", response_model=ProjectResource)
def get_project(
    projectId: int,
    service: ProjectService = Depends(get_project_service),
) -> ProjectResource:
    project = service.get_project(projectId)
    if project is None:
        raise HTTPException(status_code=404, detail=f"Unknown project {projectId}")
    return to_resource(project)


@router.delete("/{projectId}/delete", response_class=Response, status_code=200)
def delete_project(
    projectId: int,
    service: ProjectService = Depends(get_project_service),
) -> Response:
    service.delete_project(projectId)
    return Response(status_code=200)
