"""Port 002_Insert_Initial_Data.xml from Liquibase.

Liquibase's ``truncate table`` rollbacks are ported as ``DELETE FROM`` because
SQLite has no TRUNCATE statement.  Changesets 4 and 5 of the source both
declare ``truncate table project`` in their rollback, a copy/paste bug; this
downgrade deletes employee_project rows before project rows, which is the
intended behavior.
"""

from collections.abc import Sequence
from datetime import date

import sqlalchemy as sa

from alembic import op

revision: str = "0002_insert_initial_data"
down_revision: str | None = "0001_create_schema"
branch_labels: str | Sequence[str] | None = None
depends_on: str | Sequence[str] | None = None


def upgrade() -> None:
    """Insert the initial roles, employees, clients, projects, and assignments."""

    employee_role = sa.table(
        "employee_role",
        sa.column("id", sa.Integer()),
        sa.column("role", sa.String(length=30)),
    )
    op.bulk_insert(
        employee_role,
        [
            {"id": 1, "role": "Development Manager"},
            {"id": 2, "role": "Testing Manager"},
            {"id": 3, "role": "Software Developer"},
            {"id": 4, "role": "Technical Architect"},
            {"id": 5, "role": "Solutions Architect"},
            {"id": 6, "role": "Enterprise Architect"},
            {"id": 7, "role": "Data Architect"},
            {"id": 8, "role": "Integration Architect"},
            {"id": 9, "role": "Systems Architect"},
            {"id": 10, "role": "Infrastructure Architect"},
            {"id": 11, "role": "Operations Architect"},
            {"id": 12, "role": "Frontend Architect"},
            {"id": 13, "role": "Build Engineer"},
            {"id": 14, "role": "Java Developer"},
            {"id": 15, "role": "Full Stack Developer"},
            {"id": 16, "role": "Frontend Developer"},
            {"id": 17, "role": "Team Lead"},
            {"id": 18, "role": "Operations Engineer"},
            {"id": 19, "role": "Systems Administrator"},
            {"id": 20, "role": "Linux Engineer"},
            {"id": 21, "role": "DevOps Engineer"},
            {"id": 22, "role": "Database Administrator"},
            {"id": 23, "role": "Test Engineer"},
            {"id": 24, "role": "QA"},
            {"id": 25, "role": "Test Automation Engineer"},
            {"id": 26, "role": "SDET"},
            {"id": 27, "role": "Developer In Test"},
            {"id": 28, "role": "Tech Tester"},
            {"id": 29, "role": "Business Analyst"},
            {"id": 30, "role": "Product Owner"},
            {"id": 31, "role": "Scrum Master"},
            {"id": 32, "role": "Support Analyst"},
        ],
    )

    employee = sa.table(
        "employee",
        sa.column("id", sa.Integer()),
        sa.column("firstname", sa.String(length=20)),
        sa.column("surname", sa.String(length=20)),
        sa.column("employee_role_id", sa.Integer()),
    )
    op.bulk_insert(
        employee,
        [
            {"id": 1, "firstname": "Colin", "surname": "But", "employee_role_id": 3},
            {"id": 2, "firstname": "PersonA", "surname": "SurnameA", "employee_role_id": 28},
            {"id": 3, "firstname": "Firstname", "surname": "Secondname", "employee_role_id": 24},
            {"id": 4, "firstname": "Danny", "surname": "Little", "employee_role_id": 18},
        ],
    )

    client = sa.table(
        "client",
        sa.column("id", sa.Integer()),
        sa.column("client_name", sa.String(length=30)),
    )
    op.bulk_insert(
        client,
        [
            {"id": 1, "client_name": "client x"},
            {"id": 2, "client_name": "client y"},
            {"id": 3, "client_name": "client z"},
            {"id": 4, "client_name": "client a"},
            {"id": 5, "client_name": "client b"},
            {"id": 6, "client_name": "client c"},
        ],
    )

    project = sa.table(
        "project",
        sa.column("id", sa.Integer()),
        sa.column("project_title", sa.String(length=20)),
        sa.column("date_started", sa.Date()),
        sa.column("date_ended", sa.Date()),
        sa.column("client_id", sa.Integer()),
    )
    op.bulk_insert(
        project,
        [
            {
                "id": 1,
                "project_title": "project 1",
                "date_started": date(2017, 3, 15),
                "date_ended": None,
                "client_id": 1,
            },
            {
                "id": 2,
                "project_title": "government project 1",
                "date_started": date(2016, 2, 15),
                "date_ended": None,
                "client_id": 2,
            },
            {
                "id": 3,
                "project_title": "financial project",
                "date_started": date(2011, 8, 15),
                "date_ended": date(2014, 9, 3),
                "client_id": 3,
            },
            {
                "id": 4,
                "project_title": "e-commerce project",
                "date_started": date(2015, 12, 15),
                "date_ended": None,
                "client_id": 4,
            },
            {
                "id": 5,
                "project_title": "Project X",
                "date_started": date(2017, 6, 15),
                "date_ended": None,
                "client_id": 5,
            },
        ],
    )

    employee_project = sa.table(
        "employee_project",
        sa.column("employee_id", sa.Integer()),
        sa.column("project_id", sa.Integer()),
        sa.column("date_started", sa.Date()),
        sa.column("date_ended", sa.Date()),
    )
    op.bulk_insert(
        employee_project,
        [
            {
                "employee_id": 1,
                "project_id": 3,
                "date_started": date(2011, 8, 31),
                "date_ended": date(2013, 6, 15),
            },
            {
                "employee_id": 2,
                "project_id": 2,
                "date_started": date(2017, 6, 15),
                "date_ended": None,
            },
            {
                "employee_id": 3,
                "project_id": 3,
                "date_started": date(2017, 6, 15),
                "date_ended": None,
            },
            {
                "employee_id": 4,
                "project_id": 4,
                "date_started": date(2017, 6, 15),
                "date_ended": None,
            },
            {
                "employee_id": 1,
                "project_id": 5,
                "date_started": date(2017, 6, 15),
                "date_ended": None,
            },
        ],
    )


def downgrade() -> None:
    """Delete initial rows in dependency-safe order."""

    for table_name in (
        "employee_project",
        "project",
        "client",
        "employee",
        "employee_role",
    ):
        op.execute(sa.delete(sa.table(table_name)))
