# WS7 — Caching + scheduling

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Replace Ehcache + Spring cache annotations with an explicit cache abstraction (`clientFindCache`)
and replace the Quartz scheduler with APScheduler running the reporting snapshot job on the same
cadence.

## Java source references

* `src/main/resources/webapp/WEB-INF/ehcache.xml` — the `clientFindCache` definition
* `src/main/resources/META-INF/application-context-cache.xml` — `<cache:annotation-driven/>`, `EhCacheCacheManager`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/cache/ClientCachePort.java` + `impl/ClientCacheAdapter.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/repository/impl/ClientRepositoryImpl.java` — the `@Cacheable`/`@CachePut`/`@CacheEvict` usage WS2 ports
* `src/main/resources/META-INF/application-context-scheduling.xml` — `JobDetailFactoryBean`, `SimpleTriggerFactoryBean` (`repeatInterval=5000`, `startDelay=1000`), `SchedulerFactoryBean`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/scheduling/ReportingSnapshotJob.java`
* `src/main/java/com/mycompany/entapp/snowman/application/schedule/{ReportingSnapshotTask,ReportingData}.java`
* `src/main/java/com/mycompany/entapp/snowman/application/schedule/service/ReportingService.java` + `impl/ReportingServiceImpl.java`

## Requirements

### R7.1 Cache abstraction — `snowman/infrastructure/cache/`

`client_cache.py`:

```python
class ClientCache(Protocol):
    def get(self, client_id: int) -> Client | None: ...
    def put(self, client_id: int, client: Client) -> None: ...
    def evict(self, client_id: int) -> None: ...
    def clear(self) -> None: ...
```

`TTLClientCache(ClientCache)` backed by `cachetools.TLRUCache`/`TTLCache` configured from
`Settings`: `maxsize=cache_max_entries` (10000), `ttl=cache_ttl_seconds` (600). Ehcache's
`timeToIdleSeconds=300` has no direct `cachetools` equivalent — implement idle expiry by recording
last-access time per key and treating an entry older than `cache_tti_seconds` since last access as a
miss. `memoryStoreEvictionPolicy=LFU` and the `localTempSwap` disk overflow
(`maxEntriesLocalDisk=1000`) are **not** reproduced: document both as accepted simplifications in
the module docstring (heap-only, `cachetools` default eviction).

`ClientCachePort` (Protocol, `refresh_cache() -> None`) and `ClientCacheAdapter` implementing it by
calling `cache.clear()` and logging `"Executing clearing Client Cache operation"` — the port of
`@CacheEvict(allEntries = true)`. WS2 consumes `ClientCache`; WS5's `ClientCacheService` consumes
`ClientCachePort`. Expose a process-wide singleton via `get_client_cache()` in
`snowman/infrastructure/cache/deps.py` so the repository and the management endpoint share one
instance — a test must assert that.

### R7.2 Reporting service — `snowman/application/schedule/`

`reporting_data.py`: `ReportingData` dataclass with `clients: list[Client]`, `projects: list[Project]`,
`employees: list[Employee]`, `app_info: AppInfo | None`, `users: list[User]` — same field set as the
Java POJO — and a `__str__`/`__repr__` that renders all five (the Java task logs `toString()`).

`reporting_service.py`: `ReportingService(session)` with `retrieve_reporting_data() -> ReportingData`
querying all rows of each table. Java returns `null` (a TODO) — the port populates it (deviation 6).
The Java service injects `ApplicationInfoService`, `ClientService` and `UserService` and notes
"TODO project service & employee service"; the port queries all five entity types directly through
the session, keeping the intent rather than the unfinished wiring.

`reporting_snapshot_task.py`: `ReportingSnapshotTask(reporting_service)` with `execute_task()` that
logs `str(reporting_service.retrieve_reporting_data())` at INFO, exactly as the Java task does.

### R7.3 Scheduler — `snowman/infrastructure/scheduling/scheduler.py`

APScheduler `BackgroundScheduler` porting the Quartz trigger 1:1:

* one job, id `reportingSnapshotJob`, `IntervalTrigger(seconds=5, start_date=now + 1s)` — the
  `repeatInterval=5000` / `startDelay=1000` pair.
* the job body opens its own `SessionLocal()` session, builds `ReportingSnapshotTask`, runs
  `execute_task()`, and always closes the session; every exception is logged, never propagated (a
  raising job must not kill the scheduler).
* `build_scheduler()` / `start_scheduler(app)` / `shutdown_scheduler()` called from the WS0
  lifespan hook, gated on `Settings.scheduler_enabled`. `SNOWMAN_SCHEDULER_ENABLED=false` must
  fully disable it (the test harness relies on this).
* `misfire_grace_time` and `max_instances=1` set so a slow snapshot cannot pile up.

## Acceptance criteria

1. `tests/infrastructure/cache/test_client_cache.py`: put/get/evict/clear; TTL expiry
   (monkeypatched clock or a tiny configured TTL); idle expiry after `cache_tti_seconds`;
   `get_client_cache()` returns the same instance across calls.
2. `tests/application/test_reporting_service.py`: with seeded rows, `retrieve_reporting_data()`
   returns all five collections populated, and `ReportingSnapshotTask.execute_task()` emits one INFO
   log containing each collection (assert with `caplog`).
3. `tests/infrastructure/scheduling/test_scheduler.py`: the scheduler registers exactly one job with
   a 5s interval; `scheduler_enabled=False` registers none; a job body that raises is swallowed and
   logged.
4. `ClientCacheAdapter.refresh_cache()` empties the shared cache instance.
5. `ruff check .`, `mypy snowman`, `pytest` pass.

## Out of scope

Distributed/clustered caching, Quartz JDBC job stores (Java uses the in-memory store),
Hibernate second-level cache, the repository-level cache-aside wiring itself (WS2 owns it).

## Files owned

`snowman/infrastructure/cache/**`, `snowman/infrastructure/scheduling/**`,
`snowman/application/schedule/**`, `tests/infrastructure/cache/**`,
`tests/infrastructure/scheduling/**`, `tests/application/test_reporting_service.py`,
plus the scheduler start/stop calls inside the WS0 lifespan hook in `snowman/main.py`.
