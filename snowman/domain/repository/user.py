"""User repository port."""

from typing import Protocol

from snowman.domain.model.user import User


class UserDao(Protocol):
    """Persistence operations required by the user service."""

    def find_user(self, user_id: int) -> User | None:
        """Find a user by id."""

    def save_user(self, user: User) -> None:
        """Create or update a user."""

    def remove_user(self, user_id: int) -> None:
        """Delete a user by id."""
