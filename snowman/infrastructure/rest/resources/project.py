"""Project REST resource."""

from datetime import date

from pydantic import BaseModel, ConfigDict


class ProjectResource(BaseModel):
    """JSON representation of a project."""

    model_config = ConfigDict(populate_by_name=True)

    projectId: int = 0
    clientId: int | None = None
    title: str
    dateStarted: date | None = None
    dateEnded: date | None = None
