"""Integration tests for the Alembic migration chain."""

from pathlib import Path

import pytest
from alembic.autogenerate import compare_metadata
from alembic.config import Config
from alembic.migration import MigrationContext
from sqlalchemy import create_engine, func, inspect, select
from sqlalchemy.engine import Engine

from alembic import command
from snowman.config import get_settings
from snowman.db.base import Base


def _migration_config(database_url: str) -> Config:
    config = Config(str(Path(__file__).parents[1] / "alembic.ini"))
    config.set_main_option("sqlalchemy.url", database_url.replace("%", "%%"))
    return config


def _unexpected_diffs(diffs: list[object]) -> list[object]:
    unexpected: list[object] = []
    for diff in diffs:
        if (
            isinstance(diff, tuple)
            and diff
            and diff[0] in {"add_fk", "remove_fk"}
            and len(diff) > 1
            and getattr(diff[1], "table", None) is not None
            and diff[1].table.name == "employee_project"
        ):
            continue
        unexpected.append(diff)
    return unexpected


@pytest.fixture
def migrated_engine(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> Engine:
    database_url = f"sqlite:///{tmp_path / 'migrations.db'}"
    monkeypatch.setenv("SNOWMAN_DATABASE_URL", database_url)
    get_settings.cache_clear()
    config = _migration_config(database_url)
    command.upgrade(config, "head")
    engine = create_engine(database_url)
    yield engine
    engine.dispose()
    get_settings.cache_clear()


def test_migrations_schema_data_and_round_trip(
    migrated_engine: Engine,
    monkeypatch: pytest.MonkeyPatch,
) -> None:
    inspector = inspect(migrated_engine)
    actual_tables = set(inspector.get_table_names())
    assert set(Base.metadata.tables).issubset(actual_tables)
    for table_name, table in Base.metadata.tables.items():
        assert {column["name"] for column in inspector.get_columns(table_name)} == set(
            table.c.keys()
        )

    with migrated_engine.connect() as connection:
        migration_context = MigrationContext.configure(connection)
        diffs = compare_metadata(migration_context, Base.metadata)
    assert _unexpected_diffs(diffs) == []

    expected_counts = {
        "employee_role": 32,
        "employee": 4,
        "client": 6,
        "project": 5,
        "employee_project": 5,
        "app_info": 1,
        "user": 4,
    }
    with migrated_engine.connect() as connection:
        for table_name, expected_count in expected_counts.items():
            table = Base.metadata.tables[table_name]
            count = connection.scalar(select(func.count()).select_from(table))
            assert count == expected_count

    database_url = migrated_engine.url.render_as_string(hide_password=False)
    monkeypatch.setenv("SNOWMAN_DATABASE_URL", database_url)
    get_settings.cache_clear()
    config = _migration_config(database_url)
    command.downgrade(config, "base")
    command.upgrade(config, "head")
