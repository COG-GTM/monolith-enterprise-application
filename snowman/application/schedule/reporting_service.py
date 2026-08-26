"""Reporting snapshot query service."""

from sqlalchemy import select
from sqlalchemy.orm import Session

from snowman.application.schedule.reporting_data import ReportingData
from snowman.domain.model import AppInfo, Client, Employee, Project, User


class ReportingService:
    """Read all reporting entities from one database session."""

    def __init__(self, session: Session) -> None:
        self._session = session

    def retrieve_reporting_data(self) -> ReportingData:
        """Return the current contents of every reporting table."""

        clients = list(self._session.scalars(select(Client)).all())
        projects = list(self._session.scalars(select(Project)).all())
        employees = list(self._session.scalars(select(Employee)).all())
        app_info = self._session.scalars(select(AppInfo)).first()
        users = list(self._session.scalars(select(User)).all())
        return ReportingData(
            clients=clients,
            projects=projects,
            employees=employees,
            app_info=app_info,
            users=users,
        )
