"""Application boot smoke tests."""

from fastapi.testclient import TestClient

from snowman.main import create_app


def test_app_builds_and_openapi_is_available() -> None:
    with TestClient(create_app()) as client:
        response = client.get("/openapi.json")

    assert response.status_code == 200
