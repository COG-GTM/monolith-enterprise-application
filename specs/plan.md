# Snowman: Java/Spring → Python/FastAPI Migration Plan

**Status:** IN PROGRESS
**Owner:** parent/orchestrator Devin session — https://app.devin.ai/sessions/60fa76476c5f49e9b134d472db69873d
**Last updated:** 2026-08-26T20:35Z
**Specs PR:** https://github.com/COG-GTM/monolith-enterprise-application/pull/60

This file is the SINGLE SOURCE OF TRUTH for the migration. The parent session owns all writes to
this file. Child sessions MUST NOT edit it — they report their session URL, PR URL and status back
to the parent, and the parent updates the tables below.

---

## 1. Migration overview

Snowman is an Employee Management System (EMS) backend implemented as a Java 7 / Spring 4 /
Hibernate 5 monolith with a hexagonal (ports & adapters) architecture, embedded Jetty, Liquibase
migrations, JMS/ActiveMQ messaging, Ehcache caching and Quartz scheduling. The migration ports it
to Python while preserving behavioral parity: same entities, same endpoint paths and payload
shapes, same messaging contracts, same cache/schedule semantics.

### Target architecture

| Concern | Java (source) | Python (target) |
|---|---|---|
| HTTP runtime | Embedded Jetty + Spring `DispatcherServlet`, port 8090 (`-Dport`) | FastAPI + uvicorn app factory `create_app()`, port 8090 (`SNOWMAN_PORT`) |
| REST layer | `@RestController` + `*Resource` POJOs + static `*ResourceMapper` | `APIRouter` per aggregate + Pydantic v2 `*Resource` models + `mappers/*.py` |
| Domain | `domain/model`, `domain/service`, `domain/repository` | `snowman/domain/model`, `snowman/domain/service`, `snowman/domain/repository` |
| ORM | Hibernate 5 annotations | SQLAlchemy 2.x typed declarative (`Mapped[...]` / `mapped_column`) |
| Raw SQL access | `AbstractJDBCDao`, `JdbcTemplate` | SQLAlchemy `text()` on an engine-bound session |
| Migrations | Liquibase changelogs (`db/changelog/00{1..4}_*.xml`) | Alembic revisions, one per changelog |
| Messaging | Spring `JmsTemplate` + ActiveMQ queues/topic | abstract `MessageBroker` protocol + ports/adapters; in-memory broker default |
| Caching | Ehcache `clientFindCache` + Spring `@Cacheable`/`@CachePut`/`@CacheEvict` | `cachetools` LFU+TTL cache behind explicit repository cache-aside calls |
| Scheduling | Quartz `SchedulerFactoryBean`, 5s interval, 1s delay | APScheduler `BackgroundScheduler`, 5s interval, 1s delay |
| Config | `application.properties` + Spring XML | `pydantic-settings` `Settings` (`SNOWMAN_*` env vars, `.env` support) |
| Tests | JUnit 4 + Mockito (`*UTest.java`) | pytest + `TestClient`, `tests/` mirroring package layout |

### Repository layout (target)

```
snowman/                     # python package (Java sources remain untouched)
  main.py                    # create_app(), uvicorn entrypoint
  config.py                  # Settings
  db/base.py  db/session.py  # DeclarativeBase, engine, SessionLocal, get_db dependency
  domain/model/              # SQLAlchemy models
  domain/repository/         # repository protocols + impls
  domain/service/            # domain services
  domain/exception.py        # SnowmanError(BusinessError)
  application/cache/  application/healthcheck/  application/schedule/
  infrastructure/rest/{routers,resources,mappers}/
  infrastructure/messaging/{ports,adapters,converters,dto}/
  infrastructure/cache/  infrastructure/scheduling/  infrastructure/management/
alembic/  alembic.ini        # migrations
tests/                       # pytest suite
pyproject.toml               # deps + tool config (ruff, mypy, pytest)
```

### Hexagonal boundaries (must be preserved)

* `domain/` never imports from `infrastructure/`. Services depend on repository/port
  *abstractions* (typing.Protocol or ABC) only.
* `infrastructure/` provides adapters that implement the domain-facing abstractions.
* `application/` orchestrates domain + ports (cache service, health check, reporting snapshot).
* Wiring happens exclusively in `snowman/main.py` (FastAPI dependency overrides / container).

### Documented, intentional deviations from the Java behavior

These are deliberate; verification treats them as expected, not as parity defects.

1. **Request binding.** Java `EmployeeRestEndpoint`, `UserRestEndpoint` and `ProjectRestEndpoint`
   declare `@Valid EmployeeResource` *without* `@RequestBody`, so Spring binds from query/form
   params, while `ClientRestEndpoint` uses `@RequestBody` JSON. The Python port accepts a **JSON
   body** for every create/update endpoint. Uniform JSON is the target contract.
2. **`/project/update}`.** The Java mapping contains a literal stray `}`
   (`@RequestMapping("/update}")`). The port exposes `POST /project/update` and, for strict
   parity, also registers `POST /project/update}` as a hidden deprecated alias.
3. **HTTP methods on `/project/*`.** Java uses `@RequestMapping` with no `method`, so all verbs are
   accepted. The port pins verbs: `GET /project/{projectId}`, `POST /project/create`,
   `POST /project/update`, `DELETE /project/{projectId}/delete`.
4. **Unknown ids return 404.** Java dereferences a `null` entity and yields HTTP 500. The port
   raises `HTTPException(404)`. Business rule violations (`SnowmanError`) map to **500**, matching
   the Java `RuntimeException` wrapping.
5. **Primary keys on `user` and `app_info`.** The Liquibase `createTable` changesets declare no PK.
   The Alembic equivalents declare `id` as PK, required by the ORM mapping.
6. **`ReportingServiceImpl.retrieveReportingData()` returns `null` in Java (TODO).** The port
   returns a populated `ReportingData` (clients, projects, employees, users, app info).
7. **`EmployeeDaoImpl.retrieveEmployee()` returns `null` in Java (stub).** The port implements a
   real lookup by primary key.
8. **Passwords** remain plaintext in the `user` table and `UserResource`, matching the Java model.
   No hashing is introduced (out of scope; flagged as a known gap).
9. **`UserDaoImpl.saveUser()` throws `"Not Yet Implemented"` in Java.** The port implements a real
   upsert, so `POST /user/create` and `POST /user/update` succeed instead of returning 500.
10. **Messaging wire format.** Java sends JMS `ObjectMessage` (Java serialization). The port
    serializes DTOs as JSON; destinations, correlation ids and per-message metadata are preserved
    verbatim, but a Java `ObjectMessage` consumer could not read the Python payload.

---

## 2. Workstreams

Statuses: `NOT_STARTED` / `DELEGATED` / `IN_PROGRESS` / `IN_VERIFICATION` / `DONE` / `BLOCKED`.

| ID | Description | Spec | Child session | Status | PR | Last updated |
|---|---|---|---|---|---|---|
| WS0 | Foundation: packaging, settings, FastAPI app factory, SQLAlchemy base/session, Alembic init, messaging port base, pytest harness | [000-foundation.spec.md](000-foundation.spec.md) | [0d4e3a2f](https://app.devin.ai/sessions/0d4e3a2f72f44455a20a84d8ba55945d) | DELEGATED | — | 2026-08-26T20:10Z |
| WS1 | Employee slice: model, resources, mapper, repository, service, router, tests | [001-employee-slice.spec.md](001-employee-slice.spec.md) | [b950c348](https://app.devin.ai/sessions/b950c34830174b3fbc55ba2a352897ce) | DELEGATED | — | 2026-08-26T20:35Z |
| WS2 | Client slice incl. cache-aside repository + Client System REST call | [002-client-slice.spec.md](002-client-slice.spec.md) | [810f096a](https://app.devin.ai/sessions/810f096a209e4d60874a4b98d948fbad) | DELEGATED | — | 2026-08-26T20:35Z |
| WS3 | Project slice incl. `EmployeeProject` join entities | [003-project-slice.spec.md](003-project-slice.spec.md) | [6fb8705b](https://app.devin.ai/sessions/6fb8705b3d364531af58272ca8a1ab47) | DELEGATED | — | 2026-08-26T20:35Z |
| WS4 | User slice (raw-SQL DAO equivalent) | [004-user-slice.spec.md](004-user-slice.spec.md) | [8db77d23](https://app.devin.ai/sessions/8db77d2354674246aaa56bde27672da3) | DELEGATED | — | 2026-08-26T20:35Z |
| WS5 | AppInfo + management endpoints (`/app/info`, `/health`, `/cache/{name}/clear`) | [005-appinfo-management.spec.md](005-appinfo-management.spec.md) | [74a5b0fc](https://app.devin.ai/sessions/74a5b0fcb9654a118944e7909aa03f70) | DELEGATED | — | 2026-08-26T20:35Z |
| WS6 | Messaging layer: payroll/invoice/notification ports, adapters, converters, DTOs | [006-messaging.spec.md](006-messaging.spec.md) | [b423672f](https://app.devin.ai/sessions/b423672f49ba470fbee4c4ff32806d71) | DELEGATED | — | 2026-08-26T20:35Z |
| WS7 | Caching + scheduling: `clientFindCache`, APScheduler reporting snapshot | [007-cache-scheduling.spec.md](007-cache-scheduling.spec.md) | [322fd820](https://app.devin.ai/sessions/322fd8202c354a6f96a49499b050749e) | DELEGATED | — | 2026-08-26T20:35Z |
| WS8 | Alembic migrations + seed data for the 4 Liquibase changelogs | [008-migrations-seed.spec.md](008-migrations-seed.spec.md) | [0f62f040](https://app.devin.ai/sessions/0f62f04063d44c4eb8362aba8b38cf0a) | DELEGATED | — | 2026-08-26T20:35Z |

### 2.1 Dependency graph

```
WS0 (foundation)  ── hard blocker for everything else
  ├── WS1 Employee ─┐
  ├── WS2 Client ───┤  parallel; each owns disjoint files under snowman/**
  ├── WS3 Project ──┤
  ├── WS4 User ─────┤
  ├── WS5 AppInfo/management ─┘
  ├── WS6 Messaging   (parallel; converters reference domain models -- see contract note)
  ├── WS7 Cache/schedule (parallel)
  └── WS8 Migrations/seed (parallel)
```

* **Parallel after WS0:** WS1, WS2, WS3, WS4, WS5, WS6, WS7, WS8 all run concurrently.
* **Cross-workstream contract:** WS6/WS7/WS8 need domain models owned by WS1–WS4. To avoid write
  conflicts, **WS0 lands minimal model stubs for every entity** (all columns/relationships as
  specified in [001](001-employee-slice.spec.md)–[004](004-user-slice.spec.md)); WS1–WS4 then
  refine only their own model file, and WS6–WS8 import the models without editing them.
* **Dispatch deviation (2026-08-26T20:35Z):** the original plan was to wait for WS0 to merge before
  dispatching WS1–WS8. At the user's request all eight siblings were dispatched while WS0 was still
  running, so each was instructed to stack on WS0's PR branch and, failing that, to create only the
  minimum WS0-owned files and declare them. The parent resolves any duplicate scaffolding at merge.
* **File ownership:** each workstream only creates/edits files listed in its spec's *Files owned*
  section. `snowman/main.py` router/scheduler registration is the one shared file; each child
  appends its own include/registration line and must rebase on `master` before pushing.

---

## 3. Verification Log

| # | When | Scope | Checks run | Result |
|---|---|---|---|---|
| 1 | 2026-08-26T20:10Z | Specs authored (plan + WS0–WS8) | Spec completeness review against the Java tree: every model, service, endpoint, mapper, messaging port/adapter/DTO, cache, scheduler and Liquibase changeset is claimed by exactly one workstream; *Files owned* sections are disjoint | PASS — specs PR #60 opened |
| 2 | 2026-08-26T20:10Z | WS0 dispatched | Child session created via Devin API with spec path, Java refs, acceptance commands and boundary constraints | DELEGATED — awaiting PR |
| 3 | 2026-08-26T20:35Z | WS1–WS8 dispatched | 8 child sessions created in one batch with a shared structured-output schema (`workstream`, `pr_url`, `session_url`, `files_changed`, `acceptance_results`, `deviations`, `status`); each prompt carries its spec path, Java source list, file-ownership constraints and acceptance commands | DELEGATED — awaiting PRs |
| 4 | 2026-08-26T20:35Z | WS1–WS8 correction broadcast | WS0 had not landed when the siblings were dispatched (the user asked for parallel dispatch without waiting). All eight were told: branch off WS0's PR head branch once it opens, poll for it, and if it does not appear, create only the minimum WS0-owned files and list them at the top of the PR description | ACTION SENT — overlap to be resolved at merge time |

---

## 4. Remaining gaps / out of scope

* Authentication/authorization: absent in the Java app; not introduced.
* Password hashing (see deviation 8).
* Java `ClientServiceImpl` "Client System" integration builds an empty `Project` set from the
  response (dead code in Java). Ported as a faithful, clearly marked stub — no new behavior.
* Hibernate second-level cache, c3p0 connection pooling, ActiveMQ broker deployment,
  clustering/resiliency chapters of the README: infrastructure concerns, out of scope.
* `EmployeeProjectV2` / `EmployeeProjectId` are alternate Hibernate mappings of the *same*
  `employee_project` table; the port keeps one mapping plus a documented composite-key variant.
* The Java sources are **not** deleted; the Python port lands alongside them.
