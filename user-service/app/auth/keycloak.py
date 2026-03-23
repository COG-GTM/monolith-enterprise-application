"""Keycloak integration via python-keycloak.

Provides helpers for:
- fetching the realm's public key (used to verify JWTs locally)
- obtaining tokens (admin / user) for upstream calls
"""

from __future__ import annotations

import logging
from functools import lru_cache

from keycloak import KeycloakOpenID

from app.config import settings

logger = logging.getLogger(__name__)


@lru_cache(maxsize=1)
def _get_keycloak_openid() -> KeycloakOpenID:
    """Singleton KeycloakOpenID client."""
    return KeycloakOpenID(
        server_url=settings.keycloak_server_url,
        client_id=settings.keycloak_client_id,
        realm_name=settings.keycloak_realm,
        client_secret_key=settings.keycloak_client_secret,
    )


def get_keycloak_openid() -> KeycloakOpenID:
    """Public accessor so tests can mock the return value."""
    return _get_keycloak_openid()


def fetch_public_key() -> str:
    """Return the PEM-encoded RSA public key for the configured realm.

    The key is used by the JWT middleware to validate access-tokens
    without making a round-trip to Keycloak on every request.
    """
    kc = get_keycloak_openid()
    raw_key = kc.public_key()
    return (
        "-----BEGIN PUBLIC KEY-----\n"
        f"{raw_key}\n"
        "-----END PUBLIC KEY-----"
    )
