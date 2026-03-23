"""JWT token decoding and validation."""

from __future__ import annotations

import logging
from typing import Any

from jose import jwt

from app.config import settings

logger = logging.getLogger(__name__)


def decode_token(token: str, public_key: str) -> dict[str, Any]:
    """Decode and validate a Keycloak-issued JWT.

    Raises
    ------
    JWTError
        When the token is expired, has a bad signature, or is otherwise invalid.
    """
    return jwt.decode(
        token,
        public_key,
        algorithms=[settings.jwt_algorithm],
        options={"verify_aud": False},
    )
