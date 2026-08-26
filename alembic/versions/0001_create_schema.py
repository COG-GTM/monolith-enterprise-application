"""Port 001_Create_Schema.xml from Liquibase.

Liquibase's ``truncate table`` rollbacks are represented by ``DELETE FROM`` in
the data revisions because SQLite has no TRUNCATE statement.  The ``user`` and
``app_info`` primary keys are intentional deviation 5 from the source
changelog, required by the SQLAlchemy models.
"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0001_create_schema"
down_revision: str | None = None
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Create the domain and application tables."""

    op.create_table(
        "employee_role",
        sa.Column("id", sa.Integer(), primary_key=True, autoincrement=False),
        sa.Column("role", sa.String(length=30), nullable=False),
    )
    op.create_table(
        "employee",
        sa.Column("id", sa.Integer(), primary_key=True, autoincrement=True),
        sa.Column("firstname", sa.String(length=20), nullable=False),
        sa.Column("surname", sa.String(length=20), nullable=False),
        sa.Column(
            "employee_role_id",
            sa.Integer(),
            sa.ForeignKey("employee_role.id", ondelete="CASCADE"),
        ),
    )
    op.create_table(
        "client",
        sa.Column("id", sa.Integer(), primary_key=True, autoincrement=False),
        sa.Column("client_name", sa.String(length=30), nullable=False),
    )
    op.create_table(
        "project",
        sa.Column("id", sa.Integer(), primary_key=True, autoincrement=True),
        sa.Column("project_title", sa.String(length=20), nullable=False),
        sa.Column("date_started", sa.Date(), nullable=False),
        sa.Column("date_ended", sa.Date()),
        sa.Column(
            "client_id",
            sa.Integer(),
            sa.ForeignKey("client.id", ondelete="CASCADE"),
            nullable=False,
        ),
    )
    op.create_table(
        "employee_project",
        sa.Column("employee_id", sa.Integer(), primary_key=True),
        sa.Column("project_id", sa.Integer(), primary_key=True),
        sa.Column("date_started", sa.Date()),
        sa.Column("date_ended", sa.Date()),
    )
    op.create_table(
        "user",
        sa.Column("id", sa.Integer(), primary_key=True, autoincrement=False),
        sa.Column("username", sa.String(length=20)),
        sa.Column("password", sa.String(length=20)),
        sa.Column("email", sa.String(length=20)),
        sa.Column("firstname", sa.String(length=20)),
        sa.Column("secondname", sa.String(length=20)),
    )
    op.create_table(
        "app_info",
        sa.Column("id", sa.Integer(), primary_key=True, autoincrement=False),
        sa.Column("version", sa.String(length=20)),
    )


def downgrade() -> None:
    """Drop the tables in reverse dependency order."""

    op.drop_table("employee_project")
    op.drop_table("project")
    op.drop_table("client")
    op.drop_table("employee")
    op.drop_table("employee_role")
    op.drop_table("user")
    op.drop_table("app_info")
