"""User service tests."""

import pytest

from snowman.domain.exception import SnowmanError
from snowman.domain.model.user import User
from snowman.domain.service.user import UserService


class FakeUserDao:
    """In-memory DAO test double."""

    def __init__(self) -> None:
        self.users: dict[int, User] = {}
        self.find_ids: list[int] = []
        self.saved: list[User] = []
        self.removed_ids: list[int] = []

    def find_user(self, user_id: int) -> User | None:
        self.find_ids.append(user_id)
        return self.users.get(user_id)

    def save_user(self, user: User) -> None:
        self.saved.append(user)
        self.users[user.user_id] = user

    def remove_user(self, user_id: int) -> None:
        self.removed_ids.append(user_id)
        self.users.pop(user_id, None)


def test_find_user_parses_string_id() -> None:
    dao = FakeUserDao()
    expected = User(user_id=12, username="alice")
    dao.users[12] = expected

    assert UserService(dao).find_user("12") is expected
    assert dao.find_ids == [12]


def test_find_user_rejects_non_numeric_id() -> None:
    with pytest.raises(SnowmanError, match=r"^Invalid user id: abc$"):
        UserService(FakeUserDao()).find_user("abc")


def test_create_update_and_delete_delegate_to_dao() -> None:
    dao = FakeUserDao()
    service = UserService(dao)
    user = User(user_id=7, username="alice")

    service.create_user(user)
    service.update_user(user)
    service.delete_user(7)

    assert dao.saved == [user, user]
    assert dao.removed_ids == [7]
