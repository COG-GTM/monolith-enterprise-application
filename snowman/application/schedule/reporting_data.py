"""Reporting snapshot data transfer object."""

from __future__ import annotations

from dataclasses import dataclass

from snowman.domain.model import AppInfo, Client, Employee, Project, User


@dataclass
class ReportingData:
    """All collections captured by a reporting snapshot."""

    clients: list[Client]
    projects: list[Project]
    employees: list[Employee]
    app_info: AppInfo | None
    users: list[User]

    def __repr__(self) -> str:
        return (
            f"ReportingData(clients={self.clients!r}, projects={self.projects!r}, "
            f"employees={self.employees!r}, app_info={self.app_info!r}, users={self.users!r})"
        )

    def __str__(self) -> str:
        return self.__repr__()
