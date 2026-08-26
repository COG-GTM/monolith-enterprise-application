"""Port 003_Insert_App_Info.xml from Liquibase.

Liquibase's ``truncate table`` rollback is ported as ``DELETE FROM`` because
SQLite has no TRUNCATE statement.
"""

from collections.abc import Sequence

import sqlalchemy as sa

from alembic import op

revision: str = "0003_insert_app_info"
down_revision: str | None = "0002_insert_initial_data"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Insert application version information."""

    app_info = sa.table(
        "app_info",
        sa.column("id", sa.Integer()),
        sa.column("version", sa.String(length=20)),
    )
    op.bulk_insert(app_info, [{"id": 1, "version": "1.0.0"}])


def downgrade() -> None:
    """Delete application version information."""

    op.execute(sa.delete(sa.table("app_info")))
