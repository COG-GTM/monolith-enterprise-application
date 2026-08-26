"""Raw-SQL user DAO.

This ports UserDaoImpl's JdbcTemplate raw-SQL character. The real upsert in
save_user is documented deviation 9: Java throws "Not Yet Implemented".
"""

from sqlalchemy import text
from sqlalchemy.orm import Session

from snowman.domain.model.user import User

FIND_USER_SQL = text("SELECT * FROM user where id = :user_id")
SELECT_USER_SQL = text("SELECT id FROM user where id = :user_id")
INSERT_USER_SQL = text(
    "INSERT INTO user "
    "(id, username, password, email, firstname, secondname) "
    "VALUES (:user_id, :username, :password, :email, :firstname, :lastname)"
)
UPDATE_USER_SQL = text(
    "UPDATE user SET username = :username, password = :password, email = :email, "
    "firstname = :firstname, secondname = :lastname WHERE id = :user_id"
)
DELETE_USER_SQL = text("DELETE FROM user where id = :user_id")


class SqlUserDao:
    """SQLAlchemy implementation of the user repository port."""

    def __init__(self, session: Session) -> None:
        self._session = session

    def find_user(self, user_id: int) -> User | None:
        """Return a mapped user or None when no row exists."""

        row = self._session.execute(FIND_USER_SQL, {"user_id": user_id}).mappings().first()
        if row is None:
            return None
        return User(
            user_id=row["id"],
            username=row["username"],
            password=row["password"],
            email=row["email"],
            firstname=row["firstname"],
            lastname=row["secondname"],
        )

    def save_user(self, user: User) -> None:
        """Insert a new user or update an existing user."""

        params = {
            "user_id": user.user_id,
            "username": user.username,
            "password": user.password,
            "email": user.email,
            "firstname": user.firstname,
            "lastname": user.lastname,
        }
        existing = self._session.execute(SELECT_USER_SQL, {"user_id": user.user_id}).first()
        if existing is None:
            self._session.execute(INSERT_USER_SQL, params)
        else:
            self._session.execute(UPDATE_USER_SQL, params)

    def remove_user(self, user_id: int) -> None:
        """Delete a user by id."""

        self._session.execute(DELETE_USER_SQL, {"user_id": user_id})
