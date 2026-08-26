"""Port 004_Insert_Dummy_Users.xml from Liquibase.

Liquibase's ``truncate table`` rollback is ported as ``DELETE FROM`` because
SQLite has no TRUNCATE statement.
"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0004_insert_dummy_users"
down_revision: str | None = "0003_insert_app_info"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Insert the dummy users from the source changelog."""

    user = sa.table(
        "user",
        sa.column("id", sa.Integer()),
        sa.column("username", sa.String(length=20)),
        sa.column("password", sa.String(length=20)),
        sa.column("email", sa.String(length=20)),
        sa.column("firstname", sa.String(length=20)),
        sa.column("secondname", sa.String(length=20)),
    )
    op.bulk_insert(
        user,
        [
            {
                "id": 1,
                "username": "username",
                "password": "password",
                "email": "username@email.com",
                "firstname": "user first name",
                "secondname": "user second name",
            },
            {
                "id": 2,
                "username": "admin",
                "password": "admin",
                "email": "admin@email.com",
                "firstname": "admin first name",
                "secondname": "admin second name",
            },
            {
                "id": 3,
                "username": "test",
                "password": "test",
                "email": "test@email.com",
                "firstname": "test first name",
                "secondname": "test second name",
            },
            {
                "id": 4,
                "username": "dev",
                "password": "dev",
                "email": "dev@email.com",
                "firstname": "dev first name",
                "secondname": "dev second name",
            },
        ],
    )


def downgrade() -> None:
    """Delete dummy users."""

    op.execute(sa.delete(sa.table("user")))
