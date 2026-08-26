"""User domain service."""

from snowman.domain.exception import SnowmanError
from snowman.domain.model.user import User
from snowman.domain.repository.user import UserDao


class UserService:
    """Application operations for users."""

    def __init__(self, dao: UserDao) -> None:
        self._dao = dao

    def find_user(self, user_id: str) -> User | None:
        """Find a user, parsing the Java-compatible string id."""

        try:
            parsed_user_id = int(user_id)
        except ValueError as exc:
            raise SnowmanError(f"Invalid user id: {user_id}") from exc
        return self._dao.find_user(parsed_user_id)

    def create_user(self, user: User) -> None:
        """Create a user."""

        self._dao.save_user(user)

    def update_user(self, user: User) -> None:
        """Update a user without checking existence first."""

        self._dao.save_user(user)

    def delete_user(self, user_id: int) -> None:
        """Delete a user."""

        self._dao.remove_user(user_id)
