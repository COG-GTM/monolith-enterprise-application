"""User CRUD router — FastAPI equivalent of UserRestEndpoint.java.

Endpoint mapping (mirrors the Java Spring @RequestMapping):
    GET    /user/{user_id}        -> get_user          (any authenticated)
    POST   /user/create           -> create_user       (admin, manager)
    POST   /user/update           -> update_user        (admin, manager)
    DELETE /user/{user_id}/delete  -> delete_user       (admin only)

Role enforcement is done via FastAPI dependencies that replicate
Spring Security / Keycloak adapter role constraints.
"""

from __future__ import annotations

import logging

import httpx
from fastapi import APIRouter, Depends, HTTPException, status

from app.config import settings
from app.dependencies.auth import (
    get_current_user,
    require_any_authenticated,
    require_role,
)
from app.models.user import TokenUser, UserCreate, UserResponse, UserUpdate

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/user", tags=["users"])

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
_UPSTREAM = settings.upstream_base_url


def _upstream_url(path: str) -> str:
    return f"{_UPSTREAM}{path}"


# ---------------------------------------------------------------------------
# GET /user/{user_id}  — equivalent to UserRestEndpoint.getUser
# Any authenticated user may read user records.
# ---------------------------------------------------------------------------
@router.get(
    "/{user_id}",
    response_model=UserResponse,
    dependencies=[Depends(require_any_authenticated)],
)
async def get_user(
    user_id: int,
    current_user: TokenUser = Depends(get_current_user),
) -> UserResponse:
    """Fetch a single user by ID.

    Proxies to the upstream Java ``/user/{userId}`` endpoint after
    verifying the caller holds a valid Keycloak JWT.
    """
    async with httpx.AsyncClient() as client:
        resp = await client.get(_upstream_url(f"/user/{user_id}"))

    if resp.status_code == 404:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="User not found")
    if resp.status_code != 200:
        raise HTTPException(status_code=resp.status_code, detail="Upstream error")

    data = resp.json()
    return UserResponse(
        user_id=data.get("userId", 0),
        username=data.get("username", ""),
        email=data.get("email", ""),
        first_name=data.get("firstName", ""),
        second_name=data.get("secondName", ""),
    )


# ---------------------------------------------------------------------------
# POST /user/create  — equivalent to UserRestEndpoint.createNewUser
# Requires "admin" or "manager" role.
# ---------------------------------------------------------------------------
@router.post(
    "/create",
    status_code=status.HTTP_201_CREATED,
    dependencies=[Depends(require_role("admin", "manager"))],
)
async def create_user(
    payload: UserCreate,
    current_user: TokenUser = Depends(get_current_user),
) -> dict[str, str]:
    """Create a new user.

    Forwards the payload to the upstream Java ``POST /user/create`` endpoint.
    """
    async with httpx.AsyncClient() as client:
        resp = await client.post(
            _upstream_url("/user/create"),
            data={
                "username": payload.username,
                "password": payload.password,
                "email": payload.email,
                "firstName": payload.first_name,
                "secondName": payload.second_name,
            },
        )

    if resp.status_code not in (200, 201):
        raise HTTPException(status_code=resp.status_code, detail="Upstream error creating user")

    return {"status": "created"}


# ---------------------------------------------------------------------------
# POST /user/update  — equivalent to UserRestEndpoint.updateExistingUser
# Requires "admin" or "manager" role.
# ---------------------------------------------------------------------------
@router.post(
    "/update",
    dependencies=[Depends(require_role("admin", "manager"))],
)
async def update_user(
    payload: UserUpdate,
    current_user: TokenUser = Depends(get_current_user),
) -> dict[str, str]:
    """Update an existing user.

    Forwards the payload to the upstream Java ``POST /user/update`` endpoint.
    """
    async with httpx.AsyncClient() as client:
        resp = await client.post(
            _upstream_url("/user/update"),
            data={
                "userId": str(payload.user_id),
                "username": payload.username,
                "password": payload.password,
                "email": payload.email,
                "firstName": payload.first_name,
                "secondName": payload.second_name,
            },
        )

    if resp.status_code != 200:
        raise HTTPException(status_code=resp.status_code, detail="Upstream error updating user")

    return {"status": "updated"}


# ---------------------------------------------------------------------------
# DELETE /user/{user_id}/delete  — equivalent to UserRestEndpoint.deleteUser
# Requires "admin" role only.
# ---------------------------------------------------------------------------
@router.delete(
    "/{user_id}/delete",
    dependencies=[Depends(require_role("admin"))],
)
async def delete_user(
    user_id: int,
    current_user: TokenUser = Depends(get_current_user),
) -> dict[str, str]:
    """Delete a user by ID.

    Only users with the ``admin`` role may perform deletions.
    Forwards to the upstream Java ``DELETE /user/{userId}/delete`` endpoint.
    """
    async with httpx.AsyncClient() as client:
        resp = await client.delete(_upstream_url(f"/user/{user_id}/delete"))

    if resp.status_code != 200:
        raise HTTPException(status_code=resp.status_code, detail="Upstream error deleting user")

    return {"status": "deleted"}
