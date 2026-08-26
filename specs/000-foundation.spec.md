# WS0 — Foundation

**Status:** NOT_STARTED · **Blocks:** WS1–WS8 (hard dependency)

## Purpose

Stand up the Python project skeleton every other workstream builds on: packaging, settings, the
FastAPI app factory, the SQLAlchemy 2.x declarative base and session plumbing, Alembic
initialization, the abstract messaging port/broker interfaces, model stubs for all entities, and a
pytest harness. No business logic ships in WS0.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/EnterpriseApplication.java` — embedded Jetty, default
  port `8090`, port overridable via the `port` system property.
* `src/main/resources/application.properties` — `jdbc.url=jdbc:mysql://localhost:3306/snowman`,
  `jdbc.username=username`, `jdbc.password=password`, `hibernate.show_sql=true`,
  `hibernate.hbm2ddl.auto=validate`, `jms.brokerUrl=tcp://localhost:61616`.
* `src/main/resources/META-INF/application-context-db.xml` — `dataSource`, `sessionFactory`
  (`packagesToScan=...domain.model`), `HibernateTransactionManager`, `jdbcTemplate`.
* `src/main/resources/META-INF/application-context-{cache,messaging,scheduling,rest}.xml` — the
  four infrastructure modules that WS6/WS7 port.
* `src/main/resources/webapp/WEB-INF/web.xml` — `DispatcherServlet` mapped at `/`, i.e. **no
  servlet context path prefix**; all routes are rooted at `/`.
* `src/main/java/.../domain/exception/{BusinessException,SnowmanException}.java`.

## Requirements

### R0.1 Packaging

`pyproject.toml` at repo root, project name `snowman`, `requires-python = ">=3.11"`, PEP 621
metadata, hatchling backend. Runtime deps (pin with `>=` lower bounds; pick versions published at
least 7 days ago): `fastapi`, `uvicorn[standard]`, `sqlalchemy>=2.0`, `pydantic>=2`,
`pydantic-settings`, `alembic`, `apscheduler`, `cachetools`, `httpx`. Dev deps (`[project.optional-dependencies].dev`):
`pytest`, `pytest-cov`, `ruff`, `mypy`, `types-cachetools`. Configure `ruff` (line-length 100),
`mypy` (`strict = true` for the `snowman` package), and `pytest` (`testpaths = ["tests"]`) in
`pyproject.toml`. Also commit `requirements.txt` and `requirements-dev.txt` generated from those
lists so children can `pip install -r`.

### R0.2 Settings — `snowman/config.py`

`class Settings(BaseSettings)` with `model_config = SettingsConfigDict(env_prefix="SNOWMAN_",
env_file=".env", extra="ignore")` and fields:

| Field | Type | Default | Java origin |
|---|---|---|---|
| `port` | `int` | `8090` | `EnterpriseApplication.DEFAULT_PORT` |
| `database_url` | `str` | `"sqlite:///./snowman.db"` | `jdbc.url` (MySQL URL supported: `mysql+pymysql://username:password@localhost:3306/snowman`) |
| `echo_sql` | `bool` | `True` | `hibernate.show_sql` |
| `broker_url` | `str` | `"memory://"` | `jms.brokerUrl` (`tcp://localhost:61616` when a real broker is configured) |
| `scheduler_enabled` | `bool` | `True` | `application-context-scheduling.xml` |
| `client_system_url` | `str` | `"http://localhost:8080/client-system/client/{clientId}/projects"` | `ClientServiceImpl.URI` |
| `cache_max_entries` | `int` | `10000` | `ehcache.xml maxEntriesLocalHeap` |
| `cache_ttl_seconds` | `int` | `600` | `ehcache.xml timeToLiveSeconds` |
| `cache_tti_seconds` | `int` | `300` | `ehcache.xml timeToIdleSeconds` |

Expose a cached accessor `get_settings() -> Settings` (`functools.lru_cache`).

### R0.3 Database plumbing — `snowman/db/`

* `base.py`: `class Base(DeclarativeBase)` and nothing else.
* `session.py`: `engine` built from `Settings.database_url` (add
  `connect_args={"check_same_thread": False}` for SQLite), `SessionLocal = sessionmaker(bind=engine,
  autoflush=False, expire_on_commit=False)`, and a FastAPI dependency
  `get_db() -> Iterator[Session]` that commits on success, rolls back on exception, always closes.
  The commit-on-success dependency is the port of Spring's `HibernateTransactionManager`.

### R0.4 App factory — `snowman/main.py`

* `create_app() -> FastAPI` with `title="Snowman"`, `version="1.0.0"`.
* Registers exception handlers: `SnowmanError`/`BusinessError` → `500` with body
  `{"detail": "<message>"}`; `EntityNotFoundError` → `404` with `{"detail": "<message>"}`.
* Startup/shutdown lifespan hook that starts/stops the scheduler **only** when
  `Settings.scheduler_enabled` (WS7 fills in the scheduler; WS0 leaves a documented no-op hook).
* `app = create_app()` module-level, plus `def main()` running
  `uvicorn.run("snowman.main:app", host="0.0.0.0", port=get_settings().port)`, wired as the
  `snowman` console script.
* A clearly commented `# --- routers ---` block where WS1–WS5 each append exactly one
  `app.include_router(...)` line.

### R0.5 Domain exceptions — `snowman/domain/exception.py`

`class BusinessError(Exception)`, `class SnowmanError(BusinessError)`,
`class EntityNotFoundError(BusinessError)`. Ports `BusinessException`/`SnowmanException`; the
not-found type supports deviation 4 in `plan.md`.

### R0.6 Model stubs — `snowman/domain/model/`

One module per entity: `employee.py` (`Employee`, `EmployeeRole`), `client.py` (`Client`),
`project.py` (`Project`), `employee_project.py` (`EmployeeProject`), `user.py` (`User`),
`app_info.py` (`AppInfo`), re-exported from `snowman/domain/model/__init__.py`. Columns,
table names and relationships exactly as specified in specs 001–004 and 008 — WS0 lands them
complete so WS6/WS7/WS8 can import them; WS1–WS4 then refine only their own file.

### R0.7 Messaging base — `snowman/infrastructure/messaging/`

* `broker.py`: `class Destination(NamedTuple)` (`name: str`, `kind: Literal["queue", "topic"]`);
  `class Message` (`payload: Any`, `headers: dict[str, Any]`, `correlation_id: str | None`,
  `message_id: str | None`, `priority: int | None`, `expiration_ms: int | None`,
  `persistent: bool = True`); `class MessageBroker(Protocol)` with
  `def send(self, destination: Destination, message: Message) -> None`.
* `in_memory.py`: `InMemoryMessageBroker` recording sent messages in
  `dict[str, list[Message]]` and logging each send — the default broker, and the test double.
* `ports.py`: abstract ports only (`PayrollSystemPort`, `InvoiceSystemPort`, `NotificationPort` as
  `Protocol`s with the signatures in [006](006-messaging.spec.md)). WS6 implements the adapters.

### R0.8 Alembic

`alembic init alembic` with `alembic.ini` at repo root; `alembic/env.py` reads the URL from
`Settings.database_url` (never a hard-coded URL) and sets `target_metadata = Base.metadata` with
`snowman.domain.model` imported so autogenerate sees every table. WS0 ships **no** revisions —
WS8 owns all four.

### R0.9 Test harness — `tests/`

`tests/conftest.py` providing:
* `db_engine` (session-scoped): in-memory SQLite (`sqlite://` with `StaticPool`), `Base.metadata.create_all`.
* `db_session` (function-scoped): transactional session rolled back after each test.
* `client`: `TestClient(create_app())` with `get_db` overridden to `db_session` and
  `SNOWMAN_SCHEDULER_ENABLED=false`.
* `broker`: an `InMemoryMessageBroker` instance.
Plus `tests/test_app_boots.py` asserting `create_app()` builds and `GET /openapi.json` → 200.

### R0.10 CI-ready checks

`make`-free but documented in `specs/000-foundation.spec.md` and the PR body:
`ruff check .`, `mypy snowman`, `pytest`. All three must pass on an empty-slice project.

## Acceptance criteria

1. `pip install -e ".[dev]"` succeeds on Python 3.11+.
2. `ruff check .`, `mypy snowman` and `pytest` all pass.
3. `python -c "from snowman.main import create_app; create_app()"` succeeds.
4. `uvicorn snowman.main:app --port 8090` boots and `GET /openapi.json` returns 200.
5. `alembic upgrade head` is a no-op success against the default SQLite URL (no revisions yet).
6. `from snowman.domain.model import Employee, EmployeeRole, Client, Project, EmployeeProject, User, AppInfo`
   works, and `Base.metadata.tables` contains `employee`, `employee_role`, `client`, `project`,
   `employee_project`, `user`, `app_info`.
7. No module under `snowman/domain/` imports anything from `snowman/infrastructure/`
   (assert with a test that walks the AST or greps imports).

## Out of scope

Routers, services, repositories, adapters, migrations, seed data, the scheduler job body — each is
owned by WS1–WS8.

## Files owned

`pyproject.toml`, `requirements*.txt`, `.gitignore` additions, `alembic.ini`, `alembic/**`,
`snowman/{__init__,main,config}.py`, `snowman/db/**`, `snowman/domain/exception.py`,
`snowman/domain/model/**`, `snowman/infrastructure/messaging/{broker,in_memory,ports}.py`,
`tests/conftest.py`, `tests/test_app_boots.py`, `tests/test_architecture.py`.
