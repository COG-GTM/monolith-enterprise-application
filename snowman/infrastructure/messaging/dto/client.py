from pydantic import BaseModel, Field

from snowman.infrastructure.messaging.dto.project import ProjectDTO


class ClientDTO(BaseModel):
    clientId: int
    clientName: str | None = None
    projectDTOS: list[ProjectDTO] = Field(default_factory=list)
