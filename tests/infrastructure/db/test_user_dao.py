"""User DAO tests."""

from snowman.domain.model.user import User
from snowman.infrastructure.db.user_dao import (
    DELETE_USER_SQL,
    FIND_USER_SQL,
    INSERT_USER_SQL,
    SELECT_USER_SQL,
    UPDATE_USER_SQL,
    SqlUserDao,
)


def test_user_dao_uses_bound_parameters() -> None:
    statements = [FIND_USER_SQL, SELECT_USER_SQL, INSERT_USER_SQL, UPDATE_USER_SQL, DELETE_USER_SQL]
    for statement in statements:
        assert ":user_id" in statement.text
        assert "123" not in statement.text


def test_find_user_returns_none_for_unknown_id(db_session) -> None:
    assert SqlUserDao(db_session).find_user(999) is None


def test_save_find_update_and_remove_user(db_session) -> None:
    dao = SqlUserDao(db_session)
    user = User(
        user_id=1,
        username="alice",
        password="secret",
        email="alice@example.com",
        firstname="Alice",
        lastname="Example",
    )

    dao.save_user(user)
    assert dao.find_user(1).username == "alice"

    user.username = "updated"
    dao.save_user(user)
    assert dao.find_user(1).username == "updated"

    dao.remove_user(1)
    assert dao.find_user(1) is None
