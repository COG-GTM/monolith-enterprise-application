"""User REST resource.

Returning ``password`` is a deliberate Java-parity requirement (deviation 8),
not an oversight.
"""

from pydantic import BaseModel


class UserResource(BaseModel):
    """JSON representation of a user."""

    userId: int = 0
    username: str | None = None
    password: str | None = None
    email: str | None = None
    firstName: str | None = None
    secondName: str | None = None
