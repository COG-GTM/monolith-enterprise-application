# WS4 — User slice

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Port the User vertical slice. In Java, `User` is *not* a Hibernate entity: `UserDaoImpl` uses
`JdbcTemplate` with hand-written SQL. The port keeps that "raw SQL" character while using
SQLAlchemy Core/`text()` so no second data-access stack is introduced.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/domain/model/User.java` (plain POJO, no annotations)
* `src/main/java/com/mycompany/entapp/snowman/domain/service/UserService.java` + `impl/UserServiceImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/UserDao.java` + `impl/UserDaoImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/endpoint/UserRestEndpoint.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/resources/UserResource.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/mappers/UserResourceMapper.java`
* `src/main/resources/db/changelog/001_Create_Schema.xml` (changeSet 2: the `user` table)
* Tests to mirror: `.../domain/service/impl/UserServiceImplUTest.java`,
  `.../infrastructure/rest/endpoint/UserRestEndpointUTest.java`,
  `.../infrastructure/rest/mappers/UserResourceMapperUTest.java`

## Requirements

### R4.1 Model (refine the WS0 stub) — `snowman/domain/model/user.py`

The DB columns are `id, username, password, email, firstname, secondname` — note the column is
`secondname` while the Java POJO field is `lastname`. Map it explicitly:

```python
class User(Base):
    __tablename__ = "user"
    user_id: Mapped[int] = mapped_column("id", primary_key=True, autoincrement=False)
    username: Mapped[str | None] = mapped_column(String(20))
    password: Mapped[str | None] = mapped_column(String(20))
    email: Mapped[str | None] = mapped_column(String(20))
    firstname: Mapped[str | None] = mapped_column(String(20))
    lastname: Mapped[str | None] = mapped_column("secondname", String(20))
```

All columns are nullable in the DDL; keep them nullable. Passwords stay plaintext (deviation 8).

### R4.2 Resource — `snowman/infrastructure/rest/resources/user.py`

```python
class UserResource(BaseModel):
    userId: int = 0
    username: str | None = None
    password: str | None = None   # matches Java: password is returned in responses
    email: str | None = None
    firstName: str | None = None
    secondName: str | None = None
```

`GET /user/{userId}` returns the password because `UserResourceMapper.mapUserToUserResource` sets
it. That is a parity requirement, not an oversight to fix — note it in the module docstring and in
`plan.md`'s gaps list.

### R4.3 Mapper — `snowman/infrastructure/rest/mappers/user.py`

`to_resource(user)`: `userId=user.user_id`, `username`, `password`, `email`,
`firstName=user.firstname`, `secondName=user.lastname`. `to_user(resource)`: the inverse.

### R4.4 DAO — `snowman/infrastructure/db/user_dao.py`

Port `UserDaoImpl` with parameterized SQLAlchemy `text()` statements on the injected `Session` —
never f-string interpolation:

| Java | Port |
|---|---|
| `SELECT * FROM user where id = ?` | `find_user(user_id) -> User \| None`, row → `User` by the same column names; returns `None` when absent (Java's `queryForObject` throws — the router turns absence into 404) |
| `saveUser` → `throw new RuntimeException("Not Yet Implemented")` | `save_user(user)` performs a real upsert (`INSERT ... ON CONFLICT`-free: `SELECT` then `INSERT` or `UPDATE`). Java's stub means `POST /user/create` and `/user/update` currently 500; implementing them is a **documented deviation** — add it to `plan.md`'s deviation list as item 9 in your PR description and note it in the module docstring. |
| `DELETE FROM user where id = ?` | `remove_user(user_id)` |

The DAO lives under `infrastructure/`; the domain service depends on a `UserDao` Protocol declared
in `snowman/domain/repository/user.py`.

### R4.5 Service — `snowman/domain/service/user.py`

Java `UserServiceImpl` injects the DAO directly (no repository) and `findUser` takes a **String**
id, parsing it with `Integer.parseInt`. Port:

| Method | Behavior |
|---|---|
| `find_user(user_id: str) -> User \| None` | `int(user_id)`; a non-numeric id raises `SnowmanError(f"Invalid user id: {user_id}")` (Java throws `NumberFormatException` → 500; same status, clearer message) |
| `create_user(user)` | `dao.save_user(user)` |
| `update_user(user)` | `dao.save_user(user)` (Java makes no existence check — do not add one) |
| `delete_user(user_id: int)` | `dao.remove_user(user_id)` |

### R4.6 Router — `snowman/infrastructure/rest/routers/user.py`

`APIRouter(prefix="/user", tags=["user"])`:

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/user/{userId}` | path param is a **string** (Java signature) | `200` `UserResource`; `404` if unknown; `500` on non-numeric id |
| POST | `/user/create` | `UserResource` JSON | `200`, empty body |
| POST | `/user/update` | `UserResource` JSON | `200`, empty body |
| DELETE | `/user/{userId}/delete` | int path param | `200`, empty body |

## Acceptance criteria

1. Service tests cover the string-id parse path (valid + invalid) and all four methods.
2. DAO tests run against SQLite and assert the SQL is parameterized (no interpolated values) and
   that `find_user` returns `None` for an unknown id.
3. Router tests assert JSON keys `userId/username/password/email/firstName/secondName` and each
   status code, incl. `GET /user/abc` → 500.
4. `ruff check .`, `mypy snowman`, `pytest` pass.

## Out of scope

Auth, password hashing, user seed data (WS8), reporting snapshot usage of users (WS7).

## Files owned

`snowman/domain/model/user.py`, `snowman/domain/repository/user.py`,
`snowman/domain/service/user.py`, `snowman/infrastructure/db/user_dao.py`,
`snowman/infrastructure/rest/{resources,mappers,routers}/user.py`, `tests/**/*user*`,
plus one `include_router` line in `snowman/main.py`.
