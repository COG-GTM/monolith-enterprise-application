"""APScheduler integration for the reporting snapshot task."""

from __future__ import annotations

import logging
from datetime import UTC, datetime, timedelta

from apscheduler.schedulers.background import BackgroundScheduler  # type: ignore[import-untyped]
from apscheduler.triggers.interval import IntervalTrigger  # type: ignore[import-untyped]
from fastapi import FastAPI

from snowman.application.schedule import ReportingService, ReportingSnapshotTask
from snowman.config import Settings, get_settings
from snowman.db.session import SessionLocal

logger = logging.getLogger(__name__)
_scheduler: BackgroundScheduler | None = None


def _run_reporting_snapshot() -> None:
    """Run one snapshot with an independent, always-closed database session."""

    session = SessionLocal()
    try:
        ReportingSnapshotTask(ReportingService(session)).execute_task()
    except Exception:
        logger.exception("Reporting snapshot job failed")
    finally:
        session.close()


def build_scheduler(settings: Settings | None = None) -> BackgroundScheduler:
    """Build a scheduler and register the reporting job when enabled."""

    configured = settings or get_settings()
    scheduler = BackgroundScheduler()
    if configured.scheduler_enabled:
        scheduler.add_job(
            _run_reporting_snapshot,
            trigger=IntervalTrigger(
                seconds=5,
                start_date=datetime.now(UTC) + timedelta(seconds=1),
            ),
            id="reportingSnapshotJob",
            max_instances=1,
            misfire_grace_time=1,
            replace_existing=True,
        )
    return scheduler


def start_scheduler(app: FastAPI) -> None:
    """Build and start the scheduler for an application."""

    global _scheduler
    if not get_settings().scheduler_enabled or _scheduler is not None:
        return
    _scheduler = build_scheduler()
    _scheduler.start()
    app.state.reporting_scheduler = _scheduler


def shutdown_scheduler(app: FastAPI | None = None) -> None:
    """Stop the process-wide scheduler if it is running."""

    global _scheduler
    if _scheduler is not None:
        _scheduler.shutdown(wait=False)
        _scheduler = None
    if app is not None:
        app.state.reporting_scheduler = None
