"""FastAPI dependencies that replicate Spring Security role-based access.

Usage in routers:
    @router.get("/user/{user_id}", dependencies=[Depends(require_role("admin"))])
    async def get_user(user_id: int, current_user: TokenUser = Depends(get_current_user)):
        ...
"""

from __future__ import annotations

import logging
from typing import Callable

from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError

from app.auth.jwt import decode_token
from app.auth.keycloak import fetch_public_key
from app.models.user import TokenUser

logger = logging.getLogger(__name__)

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# ---------------------------------------------------------------------------
# Cached public key (refreshed on first request / after restart)
# ---------------------------------------------------------------------------
_public_key: str | None = None


def _get_public_key() -> str:
    global _public_key  # noqa: PLW0603
    if _public_key is None:
        _public_key = fetch_public_key()
    return _public_key


def reset_public_key_cache() -> None:
    """Allow tests or admin endpoints to force a key refresh."""
    global _public_key  # noqa: PLW0603
    _public_key = None


# ---------------------------------------------------------------------------
# Core dependency: extract the authenticated user from the JWT
# ---------------------------------------------------------------------------
async def get_current_user(
    token: str = Depends(oauth2_scheme),
) -> TokenUser:
    """Validate the Bearer token and return a ``TokenUser``.

    This is the FastAPI equivalent of Spring Security's
    ``@AuthenticationPrincipal KeycloakPrincipal`` injection.
    """
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        public_key = _get_public_key()
        payload = decode_token(token, public_key)
    except JWTError:
        raise credentials_exception

    # Extract roles from the Keycloak token structure
    realm_roles: list[str] = (
        payload.get("realm_access", {}).get("roles", [])
    )
    client_roles: list[str] = []
    resource_access = payload.get("resource_access", {})
    for _client, access in resource_access.items():
        client_roles.extend(access.get("roles", []))

    return TokenUser(
        sub=payload.get("sub", ""),
        preferred_username=payload.get("preferred_username"),
        email=payload.get("email"),
        realm_roles=realm_roles,
        client_roles=client_roles,
    )


# ---------------------------------------------------------------------------
# Role-based access dependencies
# ---------------------------------------------------------------------------
def require_role(*allowed_roles: str) -> Callable[..., TokenUser]:
    """Return a FastAPI dependency that enforces role membership.

    This replicates Spring Security's ``@Secured`` / ``@RolesAllowed``
    annotations, or Keycloak adapter's role-based constraints.

    Example
    -------
    >>> @router.delete("/user/{user_id}", dependencies=[Depends(require_role("admin"))])
    """

    async def _check_role(
        current_user: TokenUser = Depends(get_current_user),
    ) -> TokenUser:
        all_roles = set(current_user.realm_roles) | set(current_user.client_roles)
        if not all_roles.intersection(allowed_roles):
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail=f"One of the following roles is required: {', '.join(allowed_roles)}",
            )
        return current_user

    return _check_role


def require_any_authenticated(
    current_user: TokenUser = Depends(get_current_user),
) -> TokenUser:
    """Dependency that simply requires a valid JWT (any role)."""
    return current_user
