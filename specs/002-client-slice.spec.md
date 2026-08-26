# WS2 — Client slice

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Port the Client vertical slice: `Client` model, `ClientResource` (which embeds project resources),
mapper, cache-aware repository, the service including its "Client System" outbound REST call with
retry, and the router.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/domain/model/Client.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/service/ClientService.java` + `impl/ClientServiceImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/repository/ClientRepository.java` + `impl/ClientRepositoryImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/ClientDao.java` + `impl/ClientDaoImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/endpoint/ClientRestEndpoint.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/resources/ClientResource.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/mappers/ClientResourceMapper.java`
* Tests to mirror: `src/test/java/.../domain/service/impl/ClientServiceImplUTest.java`,
  `.../infrastructure/rest/mappers/ClientResourceMapperUTest.java`

## Requirements

### R2.1 Model (refine the WS0 stub) — `snowman/domain/model/client.py`

```python
class Client(Base):
    __tablename__ = "client"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)
    client_name: Mapped[str] = mapped_column("client_name", String(30), nullable=False)
    projects: Mapped[list["Project"]] = relationship(back_populates="client", lazy="select")
```

`autoincrement=False` matches the Liquibase DDL (`client.id INT PRIMARY KEY`, no AUTO_INCREMENT);
the Java `@GeneratedValue` on the entity contradicts the DDL — the DDL wins, ids are supplied by
the caller. Note the Java entity declares no `@Column(length)` for `client_name` while the DDL says
`VARCHAR(30)`; use 30.

### R2.2 Resource — `snowman/infrastructure/rest/resources/client.py`

```python
class ClientResource(BaseModel):
    clientId: int = 0
    clientName: str
    projects: list[ProjectResource] = Field(default_factory=list)
```

`ProjectResource` is defined by WS3; import it (do not redefine). Java's field is a `List`, so JSON
order follows insertion order of the mapped set — tests must not depend on ordering.

### R2.3 Mapper — `snowman/infrastructure/rest/mappers/client.py`

* `to_resource(client) -> ClientResource` — `clientId=client.id`, `clientName=client.client_name`,
  `projects=[project_mapper.to_resource(p) for p in client.projects]`.
* `to_client(resource) -> Client` — inverse, building `Project` objects via the WS3 project mapper.
  Java's `mapToClient` calls `ProjectResourceMapper.mapToProjects(...)` on a possibly-null list and
  would NPE; the port treats a missing/empty `projects` as `[]`.

### R2.4 Repository with cache-aside — `snowman/domain/repository/{,impl/}client.py`

```python
class ClientRepository(Protocol):
    def get_client(self, client_id: int) -> Client | None: ...
    def create_client(self, client: Client) -> None: ...
    def update_client(self, client: Client) -> None: ...
    def delete_client(self, client_id: int) -> None: ...
```

Implementation wraps the session **and** the `clientFindCache` provided by WS7
(`snowman.infrastructure.cache.ClientCache`), porting the Spring annotations exactly:

| Java annotation | Port behavior |
|---|---|
| `@Cacheable(value="clientFindCache", key="#clientId")` on `getClient` | return cached value if present, else load via `session.get(Client, client_id)` and store |
| `@CachePut(key="#client.clientId")` on `updateClient` | save, then unconditionally store the client under its id |
| `@CacheEvict(key="#clientId")` on `deleteClient` | delete, then evict that key |
| (none) on `createClient` | no cache interaction |

If WS7 has not landed yet, depend on the `ClientCache` protocol declared in
[007](007-cache-scheduling.spec.md) and inject a null-object cache in tests; do not create a second
cache implementation.

### R2.5 Service — `snowman/domain/service/client.py`

`ClientService(repository, client_system: ClientSystemGateway, )` preserving Java semantics:

* `get_client(client_id)`: load from repository; **if the loaded client has no projects**, call the
  Client System gateway (`Settings.client_system_url`, `{clientId}` substituted) and retry while the
  response status is not 200, up to `MAX_RETRIES = 3` extra attempts (Java's loop breaks when
  `retryCount > MAX_RETRIES`, i.e. at most 5 requests total: 1 initial + 4 loop iterations — replicate
  that bound exactly and cover it with a test). Then `process_response(body, client)`.
  `process_response` in Java parses the JSON, reads the `project` node, builds a `Set<Project>` with
  one empty `Project()` and **discards it** — port it as a function that parses the JSON, logs, and
  makes no mutation, with a `# Parity: Java ClientServiceImpl.processResponse discards its result`
  comment. JSON parse errors are logged, never raised.
* `create_client(client)`: `if self.get_client(client.id) is not None: raise SnowmanError("Client already exists")`, else create.
* `update_client(client)`: `if self.get_client(client.id) is None: raise SnowmanError("Client doesn't exists")`, else update.
* `delete_client(client_id)`: same guard/message as update, then delete.

Keep the message strings verbatim (`"Client doesn't exists"` typo included).

`ClientSystemGateway` is a Protocol in `snowman/domain/service/client.py` with
`def fetch_projects(self, client_id: int) -> tuple[int, str]` (status, body); its httpx-based
implementation lives in `snowman/infrastructure/rest/client_system.py`. This keeps `domain/` free of
infrastructure imports.

### R2.6 Router — `snowman/infrastructure/rest/routers/client.py`

`APIRouter(prefix="/client", tags=["client"])`:

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/client/{clientId}` | — | `200` `ClientResource`; `404` if unknown |
| POST | `/client/new` | `ClientResource` JSON | `200`, empty body; `500` on `SnowmanError` |
| POST | `/client/update` | `ClientResource` JSON | `200`, empty body; `500` on `SnowmanError` |
| DELETE | `/client/{clientId}` | — | `200`, empty body; `500` on `SnowmanError` |

Note create is `/client/new` (not `/create`) and delete shares the `GET` path with a `DELETE` verb.

## Acceptance criteria

1. Service tests cover: cache-hit path, the retry bound (assert exactly 5 gateway calls when the
   gateway never returns 200), the three guard errors with exact messages, and that
   `process_response` never mutates the client.
2. Router tests assert JSON keys `clientId/clientName/projects[]` and each status code.
3. Repository tests assert the cache-aside behaviors in R2.4 with a fake cache recording operations.
4. `ruff check .`, `mypy snowman`, `pytest` pass.

## Out of scope

The `clientFindCache` implementation (WS7), invoice messaging (WS6), client seed data (WS8).

## Files owned

`snowman/domain/model/client.py`, `snowman/domain/repository/{,impl/}client.py`,
`snowman/domain/service/client.py`, `snowman/infrastructure/rest/{resources,mappers,routers}/client.py`,
`snowman/infrastructure/rest/client_system.py`, `tests/**/*client*` (excluding cache tests),
plus one `include_router` line in `snowman/main.py`.
