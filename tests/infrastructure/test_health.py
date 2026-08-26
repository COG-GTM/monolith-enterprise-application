from sqlalchemy import create_engine
from sqlalchemy.orm import Session

from snowman.application.healthcheck import HealthCheck, HealthStatus
from snowman.infrastructure.db.health import db_status


def test_health_endpoint_is_up_when_app_info_table_exists(client) -> None:
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "UP"}


def test_health_check_is_down_when_app_info_table_is_missing() -> None:
    engine = create_engine("sqlite://")
    with Session(engine) as session:
        assert db_status(session) is False
        assert HealthCheck(session).get_health_status() is HealthStatus.DOWN
