"""Tests for reporting snapshot application services."""

from datetime import date

import pytest
from sqlalchemy.orm import Session

from snowman.application.schedule.reporting_service import ReportingService
from snowman.application.schedule.reporting_snapshot_task import ReportingSnapshotTask
from snowman.domain.model import AppInfo, Client, Employee, Project, User


def test_retrieve_reporting_data_returns_all_entity_types(db_session: Session) -> None:
    client = Client(id=1, client_name="Acme")
    db_session.add_all(
        [
            client,
            Project(
                project_title="Migration",
                date_started=date(2024, 1, 1),
                client=client,
            ),
            Employee(id=1, firstname="Ada", surname="Lovelace"),
            User(user_id=1, username="ada"),
            AppInfo(id=1, version="1.0.0"),
        ]
    )
    db_session.flush()

    data = ReportingService(db_session).retrieve_reporting_data()

    assert len(data.clients) == 1
    assert len(data.projects) == 1
    assert len(data.employees) == 1
    assert data.app_info is not None
    assert len(data.users) == 1


def test_snapshot_task_logs_all_collections(
    db_session: Session,
    caplog: pytest.LogCaptureFixture,
) -> None:
    db_session.add(AppInfo(id=1, version="1.0.0"))
    db_session.flush()
    caplog.set_level("INFO")

    ReportingSnapshotTask(ReportingService(db_session)).execute_task()

    records = [
        record
        for record in caplog.records
        if record.name.endswith("reporting_snapshot_task")
    ]
    assert len(records) == 1
    message = records[0].getMessage()
    assert all(
        collection in message
        for collection in ("clients=", "projects=", "employees=", "app_info=", "users=")
    )
