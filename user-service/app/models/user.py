"""Pydantic models that mirror the Java UserResource / User domain object."""

from pydantic import BaseModel


class UserCreate(BaseModel):
    """Payload for creating a new user."""

    username: str
    password: str
    email: str
    first_name: str
    second_name: str


class UserUpdate(BaseModel):
    """Payload for updating an existing user."""

    user_id: int
    username: str
    password: str
    email: str
    first_name: str
    second_name: str


class UserResponse(BaseModel):
    """Read-only representation returned by GET endpoints."""

    user_id: int
    username: str
    email: str
    first_name: str
    second_name: str


class TokenUser(BaseModel):
    """Minimal identity extracted from a validated JWT."""

    sub: str
    preferred_username: str | None = None
    email: str | None = None
    realm_roles: list[str] = []
    client_roles: list[str] = []
