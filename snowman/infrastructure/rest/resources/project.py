"""Port of `ProjectResource.java` (WS3-owned; minimal stand-in so WS2 can embed it)."""

from datetime import date

from pydantic import BaseModel


class ProjectResource(BaseModel):
    projectId: int = 0
    title: str
    dateStarted: date | None = None
    dateEnded: date | None = None
