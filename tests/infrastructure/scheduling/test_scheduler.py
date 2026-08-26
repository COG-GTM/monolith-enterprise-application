"""Tests for the reporting snapshot scheduler."""

import logging

import pytest
from fastapi import FastAPI

from snowman.config import Settings
from snowman.infrastructure.scheduling import scheduler as scheduler_module
from snowman.infrastructure.scheduling.scheduler import build_scheduler


def test_scheduler_registers_one_five_second_interval_job() -> None:
    scheduler = build_scheduler(Settings(scheduler_enabled=True))

    jobs = scheduler.get_jobs()

    assert len(jobs) == 1
    assert jobs[0].id == "reportingSnapshotJob"
    assert jobs[0].trigger.interval.total_seconds() == 5


def test_scheduler_disabled_registers_no_jobs() -> None:
    scheduler = build_scheduler(Settings(scheduler_enabled=False))

    assert scheduler.get_jobs() == []


def test_raising_job_body_is_swallowed_and_logged(
    monkeypatch: pytest.MonkeyPatch,
    caplog: pytest.LogCaptureFixture,
) -> None:
    class RaisingTask:
        def __init__(self, service: object) -> None:
            del service

        def execute_task(self) -> None:
            raise RuntimeError("snapshot boom")

    monkeypatch.setattr(scheduler_module, "ReportingSnapshotTask", RaisingTask)
    caplog.set_level(logging.ERROR)

    scheduler_module._run_reporting_snapshot()

    assert "snapshot boom" in caplog.text


def test_start_scheduler_stores_started_scheduler(monkeypatch: pytest.MonkeyPatch) -> None:
    settings = Settings(scheduler_enabled=False)
    monkeypatch.setattr(scheduler_module, "get_settings", lambda: settings)
    app = FastAPI()

    scheduler_module.start_scheduler(app)

    assert not hasattr(app.state, "reporting_scheduler")
