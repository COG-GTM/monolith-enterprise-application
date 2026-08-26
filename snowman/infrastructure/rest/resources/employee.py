"""Employee REST resource."""

from pydantic import BaseModel, ConfigDict


class EmployeeResource(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    employeeId: int = 0
    firstName: str
    secondName: str
    role: str | None = None
