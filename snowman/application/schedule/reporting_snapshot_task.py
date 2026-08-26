"""Scheduled reporting snapshot task."""

import logging

from snowman.application.schedule.reporting_service import ReportingService

logger = logging.getLogger(__name__)


class ReportingSnapshotTask:
    """Execute and log one reporting snapshot."""

    def __init__(self, reporting_service: ReportingService) -> None:
        self._reporting_service = reporting_service

    def execute_task(self) -> None:
        """Retrieve and log the reporting snapshot."""

        logger.info("%s", self._reporting_service.retrieve_reporting_data())
