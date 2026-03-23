"""Tests for the FastAPI auth dependencies (role-based access control)."""

from __future__ import annotations

from fastapi import FastAPI, Depends
from fastapi.testclient import TestClient

from app.dependencies.auth import (
    get_current_user,
    require_any_authenticated,
    require_role,
)
from app.models.user import TokenUser

# ---------------------------------------------------------------------------
# Helpers – override the get_current_user dependency with a fake user
# ---------------------------------------------------------------------------

_ADMIN_USER = TokenUser(
    sub="admin-uuid",
    preferred_username="admin",
    email="admin@test.com",
    realm_roles=["admin", "user"],
    client_roles=[],
)

_MANAGER_USER = TokenUser(
    sub="manager-uuid",
    preferred_username="manager",
    email="manager@test.com",
    realm_roles=["manager", "user"],
    client_roles=[],
)

_READONLY_USER = TokenUser(
    sub="reader-uuid",
    preferred_username="reader",
    email="reader@test.com",
    realm_roles=["user"],
    client_roles=[],
)


def _build_app(fake_user: TokenUser) -> FastAPI:
    """Build a tiny FastAPI app wired with the given fake identity."""
    app = FastAPI()

    async def _override() -> TokenUser:
        return fake_user

    app.dependency_overrides[get_current_user] = _override

    @app.get("/any-auth", dependencies=[Depends(require_any_authenticated)])
    async def _any() -> dict[str, str]:
        return {"ok": "true"}

    @app.get("/admin-only", dependencies=[Depends(require_role("admin"))])
    async def _admin() -> dict[str, str]:
        return {"ok": "true"}

    @app.get(
        "/admin-or-manager",
        dependencies=[Depends(require_role("admin", "manager"))],
    )
    async def _admin_manager() -> dict[str, str]:
        return {"ok": "true"}

    return app


# ---------------------------------------------------------------------------
# Tests
# ---------------------------------------------------------------------------

class TestRequireAnyAuthenticated:
    def test_allows_any_valid_user(self) -> None:
        client = TestClient(_build_app(_READONLY_USER))
        resp = client.get("/any-auth")
        assert resp.status_code == 200

    def test_allows_admin(self) -> None:
        client = TestClient(_build_app(_ADMIN_USER))
        resp = client.get("/any-auth")
        assert resp.status_code == 200


class TestRequireRole:
    def test_admin_can_access_admin_only(self) -> None:
        client = TestClient(_build_app(_ADMIN_USER))
        resp = client.get("/admin-only")
        assert resp.status_code == 200

    def test_manager_cannot_access_admin_only(self) -> None:
        client = TestClient(_build_app(_MANAGER_USER))
        resp = client.get("/admin-only")
        assert resp.status_code == 403

    def test_readonly_cannot_access_admin_only(self) -> None:
        client = TestClient(_build_app(_READONLY_USER))
        resp = client.get("/admin-only")
        assert resp.status_code == 403

    def test_admin_can_access_admin_or_manager(self) -> None:
        client = TestClient(_build_app(_ADMIN_USER))
        resp = client.get("/admin-or-manager")
        assert resp.status_code == 200

    def test_manager_can_access_admin_or_manager(self) -> None:
        client = TestClient(_build_app(_MANAGER_USER))
        resp = client.get("/admin-or-manager")
        assert resp.status_code == 200

    def test_readonly_cannot_access_admin_or_manager(self) -> None:
        client = TestClient(_build_app(_READONLY_USER))
        resp = client.get("/admin-or-manager")
        assert resp.status_code == 403
