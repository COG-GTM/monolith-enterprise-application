# Snowman: Java/Spring → Python/FastAPI Migration Plan

**Status:** IN VERIFICATION — all nine workstreams implemented and integrated; integration PR open
**Owner:** parent/orchestrator Devin session — https://app.devin.ai/sessions/60fa76476c5f49e9b134d472db69873d
**Last updated:** 2026-08-26T21:45Z
**Specs PR:** https://github.com/COG-GTM/monolith-enterprise-application/pull/60
**Integration branch:** `devin/1787774668-integration-fastapi-port` (all of #61–#69 merged + parent conflict resolutions + two defect fixes)

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
11. **`ProjectResource.clientId` is additive.** Java's `ProjectResource` carries no client, and
    `mapToProject` never sets one, while `project.client_id` is `NOT NULL` — so `POST /project/create`
    cannot create a new project in either language. The port adds an optional `clientId` field
    (honored by the mapper, echoed in responses, omissible for Java-shaped payloads) so the create
    endpoint is usable. Decided by the parent after WS3 surfaced the constraint.

---

## 2. Workstreams

Statuses: `NOT_STARTED` / `DELEGATED` / `IN_PROGRESS` / `IN_VERIFICATION` / `DONE` / `BLOCKED`.

| ID | Description | Spec | Child session | Status | PR | Last updated |
|---|---|---|---|---|---|---|
| WS0 | Foundation: packaging, settings, FastAPI app factory, SQLAlchemy base/session, Alembic init, messaging port base, pytest harness | [000-foundation.spec.md](000-foundation.spec.md) | [0d4e3a2f](https://app.devin.ai/sessions/0d4e3a2f72f44455a20a84d8ba55945d) | DONE (integrated) | [#61](https://github.com/COG-GTM/monolith-enterprise-application/pull/61) | 2026-08-26T21:45Z |
| WS1 | Employee slice: model, resources, mapper, repository, service, router, tests | [001-employee-slice.spec.md](001-employee-slice.spec.md) | [b950c348](https://app.devin.ai/sessions/b950c34830174b3fbc55ba2a352897ce) | DONE (integrated) | [#63](https://github.com/COG-GTM/monolith-enterprise-application/pull/63) | 2026-08-26T21:45Z |
| WS2 | Client slice incl. cache-aside repository + Client System REST call | [002-client-slice.spec.md](002-client-slice.spec.md) | [810f096a](https://app.devin.ai/sessions/810f096a209e4d60874a4b98d948fbad) | DONE (integrated; stale-cache defect fixed on the integration branch) | [#66](https://github.com/COG-GTM/monolith-enterprise-application/pull/66) | 2026-08-26T21:45Z |
| WS3 | Project slice incl. `EmployeeProject` join entities | [003-project-slice.spec.md](003-project-slice.spec.md) | [6fb8705b](https://app.devin.ai/sessions/6fb8705b3d364531af58272ca8a1ab47) | DONE (integrated; deviation 11 `clientId` landed at 93da132) | [#68](https://github.com/COG-GTM/monolith-enterprise-application/pull/68) | 2026-08-26T21:45Z |
| WS4 | User slice (raw-SQL DAO equivalent) | [004-user-slice.spec.md](004-user-slice.spec.md) | [8db77d23](https://app.devin.ai/sessions/8db77d2354674246aaa56bde27672da3) | DONE (integrated) | [#67](https://github.com/COG-GTM/monolith-enterprise-application/pull/67) | 2026-08-26T21:45Z |
| WS5 | AppInfo + management endpoints (`/app/info`, `/health`, `/cache/{name}/clear`) | [005-appinfo-management.spec.md](005-appinfo-management.spec.md) | [74a5b0fc](https://app.devin.ai/sessions/74a5b0fcb9654a118944e7909aa03f70) | DONE (integrated; cache-clear accepts GET+POST per Java's method-less `@RequestMapping`) | [#69](https://github.com/COG-GTM/monolith-enterprise-application/pull/69) | 2026-08-26T21:45Z |
| WS6 | Messaging layer: payroll/invoice/notification ports, adapters, converters, DTOs | [006-messaging.spec.md](006-messaging.spec.md) | [b423672f](https://app.devin.ai/sessions/b423672f49ba470fbee4c4ff32806d71) | DONE (integrated; restacked head 743ed21 merged, tests use WS0's `messages` contract) | [#62](https://github.com/COG-GTM/monolith-enterprise-application/pull/62) | 2026-08-26T21:45Z |
| WS7 | Caching + scheduling: `clientFindCache`, APScheduler reporting snapshot | [007-cache-scheduling.spec.md](007-cache-scheduling.spec.md) | [322fd820](https://app.devin.ai/sessions/322fd8202c354a6f96a49499b050749e) | DONE (integrated; real `TTLClientCache` replaces the WS2/WS5 null-object stand-ins) | [#64](https://github.com/COG-GTM/monolith-enterprise-application/pull/64) | 2026-08-26T21:45Z |
| WS8 | Alembic migrations + seed data for the 4 Liquibase changelogs | [008-migrations-seed.spec.md](008-migrations-seed.spec.md) | [0f62f040](https://app.devin.ai/sessions/0f62f04063d44c4eb8362aba8b38cf0a) | DONE (integrated) | [#65](https://github.com/COG-GTM/monolith-enterprise-application/pull/65) | 2026-08-26T21:45Z |

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
| 5 | 2026-08-26T21:05Z | All nine children reported | Structured output reviewed for every workstream. Each child ran its own slice tests, `ruff check .`, `mypy snowman` and `create_app()`; WS8 additionally ran `alembic upgrade head` / `downgrade base` / `upgrade head`. Eight PRs opened (#61–#69). Cross-cutting findings: (a) WS1–WS5, WS7, WS8 correctly stacked on WS0's branch `devin/1787773226-ws0-foundation-fastapi`; (b) WS6 (#62) stayed on master with duplicate WS0-owned stand-ins; (c) WS2 created WS3-owned `resources/project.py` / `mappers/project.py` stand-ins; (d) WS2 and WS5 wired null-object cache stand-ins pending WS7; (e) WS3 surfaced the `POST /project/create` NOT NULL `client_id` contract gap | PARTIAL — per-slice checks pass in isolation; nothing verified integrated yet |
| 6 | 2026-08-26T21:05Z | Defect dispatch + contract decision | WS6 asked to restack on WS0's branch and delete its 15 WS0-owned stand-ins; WS3 asked to implement deviation 11 (optional additive `ProjectResource.clientId`) so `POST /project/create` works. WS2's project stand-ins and both null-object cache adapters are resolved by the parent on the integration branch (WS3's files and WS7's real `TTLClientCache` win) | FIXES DISPATCHED |
| 7 | 2026-08-26T21:20Z | Integration branch build | All eight branches merged onto WS0's branch with parent-decided conflict rules, then the full gate: `ruff check .`, `mypy snowman`, full `pytest`, `alembic upgrade head` on sqlite, `create_app()`, live `uvicorn` boot on port 8090 and per-endpoint curl checks against seeded data | PASS at 23b58e7 — `ruff` clean, `mypy` clean (91 files), `pytest` 118 passed, all four Alembic revisions apply with the Liquibase seed row counts, `GET /health` `{"status":"UP"}`, `GET /app/info` `{"id":1,"version":"1.0.0"}`, `GET /employee/1`, `/client/1` (nested project), `/project/1`, `/user/1` all 200 |
| 8 | 2026-08-26T21:30Z | Parent hands-on API experiment against the running integrated app (seeded sqlite, uvicorn :8090) | Full employee lifecycle (`POST /employee/create` → `GET` → `POST /employee/update` → `DELETE /employee/{id}/delete` → `GET` 404); unknown ids on every aggregate; invalid role rejected; client lifecycle on Java's real paths (`POST /client/new`, `POST /client/update`, `DELETE /client/{clientId}`); `POST /user/create` + read-back; `POST /cache/clientFindCache/clear`; the hidden `POST /project/update}` alias (deviation 2); `GET /openapi.json` route inventory | PARTIAL — parity confirmed for all of the above (404s, role validation, alias, cache-clear response body). **Two defects found:** (a) `GET /client/{id}` returned 500 `DetachedInstanceError` on the next read after `POST /client/update`, because the repository cached an ORM instance whose lazy `projects` collection could no longer load once the request session closed — a cache clear "fixed" it, confirming the cache as the source; (b) `POST /project/create` 500'd on the `NOT NULL project.client_id` constraint (the deviation-11 gap) |
| 9 | 2026-08-26T21:40Z | Defect fixes + final integrated gate | (a) client repository now forces `projects` to load and expunges the client+projects before caching, keeping Java's cache semantics (get = aside, update = unconditional put, delete = evict, create = no cache) with a real-cache regression test load→close→update→load; (b) WS0's `InMemoryMessageBroker.sent` alias removed — it existed only for WS6's tests, which now use the `messages` contract; (c) WS3's deviation-11 `clientId` and WS6's restacked head merged. Re-ran `ruff check .`, `mypy snowman`, `pytest`, and re-verified the endpoints against a restarted server | PASS at ef68c86 — `ruff` clean, `mypy` clean (91 files), `pytest` 122 passed; the client update→read sequence now returns the updated client (200) on both the cached and post-clear read; `POST /project/create` with `clientId` returns 200 and persists with `clientId` on read-back. Known remaining behavior: `POST /project/create` **without** `clientId` still 500s on the NOT NULL constraint — kept deliberately, since Java's `ProjectResource`/mapper have no client field at all (deviation 11 is additive only) |

---

## 4. Remaining gaps / out of scope

* `POST /project/create` without the additive `clientId` fails on the `NOT NULL project.client_id`
  constraint. This mirrors Java, whose `ProjectResourceMapper.mapToProject` leaves the client unset
  (`//project.setClient`); deviation 11 adds the optional field so the endpoint is usable, but the
  Java-shaped payload is preserved as-is rather than being papered over.
* `POST /project/create` honours the client-supplied `projectId` (Java's mapper does the same via
  `project.setId(projectResource.getProjectId())`), so it is an upsert-by-id rather than a
  server-generated identity, despite the entity's `@GeneratedValue`.

* Authentication/authorization: absent in the Java app; not introduced.
* Password hashing (see deviation 8).
* Java `ClientServiceImpl` "Client System" integration builds an empty `Project` set from the
  response (dead code in Java). Ported as a faithful, clearly marked stub — no new behavior.
* Hibernate second-level cache, c3p0 connection pooling, ActiveMQ broker deployment,
  clustering/resiliency chapters of the README: infrastructure concerns, out of scope.
* `EmployeeProjectV2` / `EmployeeProjectId` are alternate Hibernate mappings of the *same*
  `employee_project` table; the port keeps one mapping plus a documented composite-key variant.
* The Java sources are **not** deleted; the Python port lands alongside them.
