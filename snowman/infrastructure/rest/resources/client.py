"""Port of `infrastructure/rest/resources/ClientResource.java`."""

from pydantic import BaseModel, Field

from snowman.infrastructure.rest.resources.project import ProjectResource


class ClientResource(BaseModel):
    clientId: int = 0
    clientName: str
    projects: list[ProjectResource] = Field(default_factory=list)
