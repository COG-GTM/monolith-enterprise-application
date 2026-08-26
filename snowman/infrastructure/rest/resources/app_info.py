from pydantic import BaseModel


class AppInfoResource(BaseModel):
    id: int
    version: str | None
