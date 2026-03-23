"""Tests for JWT decoding logic."""

from __future__ import annotations

import time

import pytest
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa
from jose import jwt as jose_jwt

from app.auth.jwt import decode_token

# ---------------------------------------------------------------------------
# Generate a fresh RSA key-pair for testing
# ---------------------------------------------------------------------------

_private_key_obj = rsa.generate_private_key(public_exponent=65537, key_size=2048)

_TEST_PRIVATE_KEY = _private_key_obj.private_bytes(
    serialization.Encoding.PEM,
    serialization.PrivateFormat.TraditionalOpenSSL,
    serialization.NoEncryption(),
).decode()

_TEST_PUBLIC_KEY = _private_key_obj.public_key().public_bytes(
    serialization.Encoding.PEM,
    serialization.PublicFormat.SubjectPublicKeyInfo,
).decode()


def _make_token(payload: dict, key: str = _TEST_PRIVATE_KEY) -> str:
    return jose_jwt.encode(payload, key, algorithm="RS256")


class TestDecodeToken:
    def test_valid_token(self) -> None:
        token = _make_token(
            {
                "sub": "user-123",
                "preferred_username": "johndoe",
                "exp": int(time.time()) + 3600,
            }
        )
        result = decode_token(token, _TEST_PUBLIC_KEY)
        assert result["sub"] == "user-123"
        assert result["preferred_username"] == "johndoe"

    def test_expired_token_raises(self) -> None:
        token = _make_token(
            {
                "sub": "user-123",
                "exp": int(time.time()) - 10,
            }
        )
        with pytest.raises(Exception):
            decode_token(token, _TEST_PUBLIC_KEY)

    def test_bad_signature_raises(self) -> None:
        token = _make_token(
            {
                "sub": "user-123",
                "exp": int(time.time()) + 3600,
            }
        )
        # Corrupt the token signature
        parts = token.rsplit(".", 1)
        bad_token = parts[0] + ".invalidsignaturedata"
        with pytest.raises(Exception):
            decode_token(bad_token, _TEST_PUBLIC_KEY)
