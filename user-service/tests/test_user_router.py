"""Tests for the /user router endpoints with mocked upstream and auth."""

from __future__ import annotations

from unittest.mock import AsyncMock, patch

from fastapi.testclient import TestClient

from app.dependencies.auth import get_current_user
from app.main import app
from app.models.user import TokenUser

# ---------------------------------------------------------------------------
# Fake users
# ---------------------------------------------------------------------------

_ADMIN = TokenUser(
    sub="admin-uuid",
    preferred_username="admin",
    email="admin@test.com",
    realm_roles=["admin"],
    client_roles=[],
)

_MANAGER = TokenUser(
    sub="mgr-uuid",
    preferred_username="manager",
    email="manager@test.com",
    realm_roles=["manager"],
    client_roles=[],
)

_VIEWER = TokenUser(
    sub="viewer-uuid",
    preferred_username="viewer",
    email="viewer@test.com",
    realm_roles=["user"],
    client_roles=[],
)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

class _FakeResponse:
    """Minimal stand-in for httpx.Response."""

    def __init__(self, status_code: int, json_data: dict | None = None) -> None:
        self.status_code = status_code
        self._json = json_data or {}

    def json(self) -> dict:
        return self._json


def _override_user(user: TokenUser):
    async def _dep() -> TokenUser:
        return user
    return _dep


def _client_as(user: TokenUser) -> TestClient:
    app.dependency_overrides[get_current_user] = _override_user(user)
    return TestClient(app)


# ---------------------------------------------------------------------------
# GET /user/{user_id}
# ---------------------------------------------------------------------------

class TestGetUser:
    def teardown_method(self) -> None:
        app.dependency_overrides.clear()

    @patch("app.routers.users.httpx.AsyncClient")
    def test_authenticated_user_can_read(self, mock_client_cls: AsyncMock) -> None:
        upstream_json = {
            "userId": 1,
            "username": "jdoe",
            "email": "jdoe@example.com",
            "firstName": "John",
            "secondName": "Doe",
        }
        mock_resp = _FakeResponse(200, upstream_json)

        mock_instance = AsyncMock()
        mock_instance.get = AsyncMock(return_value=mock_resp)
        mock_instance.__aenter__ = AsyncMock(return_value=mock_instance)
        mock_instance.__aexit__ = AsyncMock(return_value=False)
        mock_client_cls.return_value = mock_instance

        client = _client_as(_VIEWER)
        resp = client.get("/user/1")

        assert resp.status_code == 200
        body = resp.json()
        assert body["username"] == "jdoe"
        assert body["user_id"] == 1

    @patch("app.routers.users.httpx.AsyncClient")
    def test_upstream_404_returns_404(self, mock_client_cls: AsyncMock) -> None:
        mock_resp = _FakeResponse(404)

        mock_instance = AsyncMock()
        mock_instance.get = AsyncMock(return_value=mock_resp)
        mock_instance.__aenter__ = AsyncMock(return_value=mock_instance)
        mock_instance.__aexit__ = AsyncMock(return_value=False)
        mock_client_cls.return_value = mock_instance

        client = _client_as(_ADMIN)
        resp = client.get("/user/999")
        assert resp.status_code == 404


# ---------------------------------------------------------------------------
# POST /user/create
# ---------------------------------------------------------------------------

class TestCreateUser:
    def teardown_method(self) -> None:
        app.dependency_overrides.clear()

    @patch("app.routers.users.httpx.AsyncClient")
    def test_admin_can_create(self, mock_client_cls: AsyncMock) -> None:
        mock_resp = _FakeResponse(200)

        mock_instance = AsyncMock()
        mock_instance.post = AsyncMock(return_value=mock_resp)
        mock_instance.__aenter__ = AsyncMock(return_value=mock_instance)
        mock_instance.__aexit__ = AsyncMock(return_value=False)
        mock_client_cls.return_value = mock_instance

        client = _client_as(_ADMIN)
        resp = client.post(
            "/user/create",
            json={
                "username": "newuser",
                "password": "secret",
                "email": "new@example.com",
                "first_name": "New",
                "second_name": "User",
            },
        )
        assert resp.status_code == 201

    def test_viewer_cannot_create(self) -> None:
        client = _client_as(_VIEWER)
        resp = client.post(
            "/user/create",
            json={
                "username": "newuser",
                "password": "secret",
                "email": "new@example.com",
                "first_name": "New",
                "second_name": "User",
            },
        )
        assert resp.status_code == 403


# ---------------------------------------------------------------------------
# DELETE /user/{user_id}/delete
# ---------------------------------------------------------------------------

class TestDeleteUser:
    def teardown_method(self) -> None:
        app.dependency_overrides.clear()

    @patch("app.routers.users.httpx.AsyncClient")
    def test_admin_can_delete(self, mock_client_cls: AsyncMock) -> None:
        mock_resp = _FakeResponse(200)

        mock_instance = AsyncMock()
        mock_instance.delete = AsyncMock(return_value=mock_resp)
        mock_instance.__aenter__ = AsyncMock(return_value=mock_instance)
        mock_instance.__aexit__ = AsyncMock(return_value=False)
        mock_client_cls.return_value = mock_instance

        client = _client_as(_ADMIN)
        resp = client.delete("/user/1/delete")
        assert resp.status_code == 200

    def test_manager_cannot_delete(self) -> None:
        client = _client_as(_MANAGER)
        resp = client.delete("/user/1/delete")
        assert resp.status_code == 403

    def test_viewer_cannot_delete(self) -> None:
        client = _client_as(_VIEWER)
        resp = client.delete("/user/1/delete")
        assert resp.status_code == 403
