from pydantic import BaseModel, Field

from snowman.infrastructure.messaging.dto.project import ProjectDTO


class EmployeeDTO(BaseModel):
    id: int
    firstName: str | None = None
    surname: str | None = None
    role: str | None = None
    projectDTOList: list[ProjectDTO] = Field(default_factory=list)
