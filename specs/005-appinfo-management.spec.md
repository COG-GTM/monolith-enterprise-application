# WS5 — AppInfo + management endpoints

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Port `AppInfo` (loaded once at startup into an in-memory map), the `/app/info` endpoint, the DB
health check behind `/health`, and the cache-management endpoint `/cache/{cacheName}/clear`.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/domain/model/AppInfo.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/service/ApplicationInfoService.java` + `impl/ApplicationInfoServiceImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/repository/ApplicationInfoRepository.java` + `impl/ApplicationInfoRepositoryImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/ApplicationInfoDao.java` + `impl/ApplicationInfoDaoImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/endpoint/AppInfoRestEndpoint.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/resources/AppInfoResource.java` + `mappers/AppInfoResourceMapper.java`
* `src/main/java/com/mycompany/entapp/snowman/application/healthcheck/{HealthCheck,HealthStatus}.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/health/DBHealthCheck.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/management/{HealthCheckRestEndpoint,CacheManagementRestEndpoint}.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/management/resource/StatusResource.java`
* `src/main/java/com/mycompany/entapp/snowman/application/cache/ClientCacheService.java` + `impl/ClientCacheServiceImpl.java`
* Tests to mirror: `.../domain/repository/impl/ApplicationInfoRepositoryImplUTest.java`,
  `.../domain/service/impl/ApplicationInfoServiceImplUTest.java`,
  `.../infrastructure/rest/endpoint/AppInfoRestEndpointUTest.java`

## Requirements

### R5.1 Model (refine the WS0 stub) — `snowman/domain/model/app_info.py`

```python
class AppInfo(Base):
    __tablename__ = "app_info"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)
    version: Mapped[str | None] = mapped_column(String(20))
```

(The DDL declares no PK; the ORM requires one — deviation 5.)

### R5.2 DAO + repository

* `snowman/infrastructure/db/app_info_dao.py`: `load_application_infos() -> list[AppInfo]` executing
  `SELECT * FROM app_info` (Java uses raw JDBC here, so use `text()`, not the ORM query API).
  Java swallows `SQLException` and returns an empty list — the port logs the exception and returns
  `[]` rather than propagating.
* `snowman/domain/repository/impl/app_info.py`: `ApplicationInfoRepository` with an in-memory
  `dict[int, AppInfo]` populated by `initialize()`, the port of the `@PostConstruct`-loaded map.
  `initialize()` is called from the FastAPI lifespan startup hook in `snowman/main.py`; it must be
  idempotent and must not raise when the table is missing or empty (Java logs and continues).
  `get_app_info_map() -> dict[int, AppInfo]` returns the cached map — **not** a fresh query.

### R5.3 Service — `snowman/domain/service/app_info.py`

`get_app_info() -> AppInfo` porting `ApplicationInfoServiceImpl` exactly:

* map empty/`None` → `BusinessError("AppInfo is null or empty")`
* `len(map) != 1` → `BusinessError("There are more than one entry in AppInfo")`
* else the single value.

Both messages verbatim (`ApplicationInfoServiceImplUTest` asserts them). Per the WS0 exception
handler, a `BusinessError` surfaces as HTTP 500, matching the Java
`throw new RuntimeException(e)` in the endpoint.

### R5.4 Health check

* `snowman/infrastructure/db/health.py`: `db_status(session) -> bool` executing
  `SELECT min(1) FROM app_info`, returning `True` when a row comes back, `False` on any
  `SQLAlchemyError` (Java logs and returns false).
* `snowman/application/healthcheck.py`: `HealthStatus` = `enum.Enum` with `UP = "UP"`,
  `DOWN = "DOWN"`; `HealthCheck.get_health_status()` → `UP` if `db_status()` else `DOWN`.

### R5.5 Cache management service

`snowman/application/cache/client_cache_service.py`: `ClientCacheService(cache_port: ClientCachePort)`
with `clear_cache()` that logs `"Clearing Client Cache"`, calls `cache_port.refresh_cache()`, logs
`"Cleared Client Cache"`. `ClientCachePort` and its adapter are owned by WS7; depend on the Protocol
declared in [007](007-cache-scheduling.spec.md) and use a fake in tests.

### R5.6 Routers

`snowman/infrastructure/rest/routers/app_info.py` — `APIRouter(tags=["app"])`:

| Method | Path | Response |
|---|---|---|
| GET | `/app/info` | `200` `{"id": int, "version": str}`; `500` when the AppInfo map is empty or has >1 entry |

`snowman/infrastructure/management/router.py` — `APIRouter(tags=["management"])`:

| Method | Path | Response |
|---|---|---|
| GET | `/health` | `200` `{"status": "UP"}` or `{"status": "DOWN"}` — always 200, the status is in the body (Java returns `ResponseEntity.ok` unconditionally) |
| GET | `/cache/{cacheName}/clear` | `200` `{"statusCode": 200, "description": "<cacheName> have been cleared"}` |

The `"<name> have been cleared"` wording (including the grammar) and the `statusCode`/`description`
keys are contract; `CacheManagementRestEndpoint` uses `@RequestMapping` with no method, so GET is
the pinned verb.

## Acceptance criteria

1. `GET /app/info` returns `{"id": 1, "version": "1.0.0"}` after WS8 seed data is applied; with an
   empty `app_info` table it returns 500 with detail `"AppInfo is null or empty"`, and with two rows
   `"There are more than one entry in AppInfo"`.
2. `GET /health` returns `{"status": "UP"}` against a migrated DB and `{"status": "DOWN"}` when the
   `app_info` table does not exist.
3. `GET /cache/clientFindCache/clear` returns the exact body in R5.6 and invokes the cache port once.
4. The AppInfo map is loaded exactly once at startup — a test asserts the DAO is called once across
   two `/app/info` requests.
5. `ruff check .`, `mypy snowman`, `pytest` pass.

## Out of scope

The cache implementation itself (WS7), `app_info` seed data (WS8), Spring Boot Actuator-style
extra endpoints (not present in Java).

## Files owned

`snowman/domain/model/app_info.py`, `snowman/domain/repository/{,impl/}app_info.py`,
`snowman/domain/service/app_info.py`, `snowman/infrastructure/db/{app_info_dao,health}.py`,
`snowman/application/healthcheck.py`, `snowman/application/cache/client_cache_service.py`,
`snowman/infrastructure/rest/{resources,mappers,routers}/app_info.py`,
`snowman/infrastructure/management/router.py`, `tests/**/*app_info*`, `tests/**/*health*`,
`tests/**/*cache_management*`, plus the `include_router` lines and the `initialize()` startup call
in `snowman/main.py`.
