import enum
from collections.abc import Callable

from sqlalchemy.orm import Session

from snowman.infrastructure.db.health import db_status


class HealthStatus(enum.Enum):
    UP = "UP"
    DOWN = "DOWN"


class HealthCheck:
    def __init__(
        self,
        session: Session,
        status_checker: Callable[[Session], bool] = db_status,
    ) -> None:
        self._session = session
        self._status_checker = status_checker

    def get_health_status(self) -> HealthStatus:
        if self._status_checker(self._session):
            return HealthStatus.UP
        return HealthStatus.DOWN
