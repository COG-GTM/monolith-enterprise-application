from collections.abc import Iterable
from datetime import date
from typing import Protocol


class ProjectLike(Protocol):
    id: int
    project_title: str | None
    date_started: date | None
    date_ended: date | None


class RoleLike(Protocol):
    role: str


class EmployeeProjectLike(Protocol):
    project: ProjectLike


class EmployeeLike(Protocol):
    id: int
    firstname: str | None
    surname: str | None
    role: RoleLike | None
    projects: Iterable[EmployeeProjectLike]


class ClientLike(Protocol):
    id: int
    client_name: str | None
    projects: Iterable[ProjectLike]
