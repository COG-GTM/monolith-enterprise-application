from datetime import date

from pydantic import BaseModel


class ProjectDTO(BaseModel):
    projectId: int
    projectTitle: str | None = None
    dateStarted: date | None = None
    dateEnded: date | None = None
