# WS1 — Employee slice

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Port the Employee vertical slice — `Employee`/`EmployeeRole` models, REST resource + mapper,
repository, service and router — preserving endpoint paths, payload field names and service-level
business rules.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/domain/model/Employee.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/model/EmployeeRole.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/service/EmployeeService.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/service/impl/EmployeeServiceImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/repository/EmployeeRepository.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/repository/impl/EmployeeRepositoryImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/EmployeeDao.java` + `impl/EmployeeDaoImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/endpoint/EmployeeRestEndpoint.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/resources/EmployeeResource.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/mappers/EmployeeResourceMapper.java`
* Tests to mirror: `src/test/java/.../domain/service/impl/EmployeeServiceImplUTest.java`,
  `.../infrastructure/rest/endpoint/EmployeeRestEndpointUTest.java`,
  `.../infrastructure/rest/mappers/EmployeeResourceMapperUTest.java`

## Requirements

### R1.1 Models (refine the WS0 stubs)

`snowman/domain/model/employee.py`:

```python
class EmployeeRole(Base):
    __tablename__ = "employee_role"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=False)   # Java @Id, no @GeneratedValue
    role: Mapped[str] = mapped_column(String(30), nullable=False)

class Employee(Base):
    __tablename__ = "employee"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)    # @GeneratedValue(AUTO)
    firstname: Mapped[str] = mapped_column(String(20), nullable=False)
    surname: Mapped[str] = mapped_column(String(20), nullable=False)
    employee_role_id: Mapped[int | None] = mapped_column(ForeignKey("employee_role.id", ondelete="CASCADE"))
    role: Mapped[EmployeeRole | None] = relationship(lazy="joined")           # @OneToOne
    projects: Mapped[list["EmployeeProject"]] = relationship(back_populates="employee", lazy="select")
```

`employee.projects` is the port of `Set<EmployeeProject> projects` mapped by `EmployeeProject.employee`
(the commented-out `@ManyToMany` in Java is dead code and is **not** ported). `EmployeeProject` itself
is owned by WS3.

### R1.2 Resource — `snowman/infrastructure/rest/resources/employee.py`

Pydantic v2 model, field names exactly as the Java resource serializes them:

```python
class EmployeeResource(BaseModel):
    employeeId: int = 0
    firstName: str
    secondName: str
    role: str | None = None
```

`model_config = ConfigDict(populate_by_name=True)`. Do **not** rename to snake_case — the JSON keys
are part of the contract. `# TODO add Projects` in the Java resource means projects are absent from
the payload; keep them absent.

### R1.3 Mapper — `snowman/infrastructure/rest/mappers/employee.py`

Module-level functions (the Java class is a static-only final class):

* `to_resource(employee: Employee) -> EmployeeResource` — `employeeId=employee.id`,
  `firstName=employee.firstname`, `secondName=employee.surname`, `role=employee.role.role`.
  The Java mapper dereferences `getRole()` unconditionally; the port must instead emit `None`
  when `employee.role is None` (NPE-avoidance is not a behavior change any client can observe).
* `to_employee(resource: EmployeeResource) -> Employee` — sets `id`, `firstname`, `surname`, and a
  **new detached** `EmployeeRole(role=resource.role)` exactly as Java does (no id resolution).
  Repository code is responsible for resolving the role to an existing row by `role` name before
  persisting; if no such role exists, raise `SnowmanError(f"Unknown employee role: {role}")`.

### R1.4 Repository — `snowman/domain/repository/employee.py`

```python
class EmployeeRepository(Protocol):
    def find_employee(self, employee_id: int) -> Employee | None: ...
    def save_employee(self, employee: Employee) -> None: ...
    def remove_employee(self, employee_id: int) -> None: ...
```

`snowman/domain/repository/impl/employee.py` — `SqlAlchemyEmployeeRepository(session: Session)`:
`find_employee` → `session.get(Employee, employee_id)` (Java's DAO returns `null`; deviation 7 in
`plan.md` makes this a real lookup); `save_employee` → `session.merge` + `flush` (Java `save`,
used for both create and update); `remove_employee` → load then `session.delete` (Java deletes a
bare int, which is a bug; delete the entity).

### R1.5 Service — `snowman/domain/service/employee.py`

`EmployeeService(repository: EmployeeRepository)` with exactly the Java semantics:

| Method | Java behavior to preserve |
|---|---|
| `get_employee(employee_id)` | delegate to `find_employee` |
| `create_employee(employee)` | unconditional `save_employee` (no existence check) |
| `update_employee(employee)` | if `get_employee(employee.id)` is falsy → raise `EntityNotFoundError(f"There is no existing employee with id: {employee.id}")`, else save |
| `delete_employee(employee_id)` | if not found → raise `EntityNotFoundError(f"There is no existing employee with id: {employee_id}")`, else remove |

Keep the message strings verbatim — the Java unit tests assert on them.

### R1.6 Router — `snowman/infrastructure/rest/routers/employee.py`

`APIRouter(prefix="/employee", tags=["employee"])`:

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/employee/{employeeId}` | — | `200` `EmployeeResource`; `404` if unknown |
| POST | `/employee/create` | `EmployeeResource` JSON body | `200`, empty body |
| POST | `/employee/update` | `EmployeeResource` JSON body | `200`, empty body; `404` if unknown |
| DELETE | `/employee/{employeeId}/delete` | — | `200`, empty body; `404` if unknown |

Note the path shapes: delete is `{employeeId}/delete`, not `/delete/{employeeId}`. Empty-body 200s
are the port of `ResponseEntity.ok().build()` — declare `response_class=Response`,
`status_code=200`. Register in `snowman/main.py` under the `# --- routers ---` block.

## Acceptance criteria

1. `tests/domain/service/test_employee_service.py` covers all four service methods including both
   error paths, with a stub repository (mirrors `EmployeeServiceImplUTest`).
2. `tests/infrastructure/rest/test_employee_router.py` exercises all four endpoints through
   `TestClient` against SQLite, asserting exact JSON keys `employeeId/firstName/secondName/role`,
   the 404s, and empty 200 bodies.
3. `tests/infrastructure/rest/test_employee_mapper.py` asserts round-trip mapping incl. the
   `role is None` case.
4. `ruff check .`, `mypy snowman`, `pytest` pass.
5. `GET /openapi.json` lists exactly the four paths above for the employee router.

## Out of scope

`EmployeeProject` mapping (WS3), payroll messaging (WS6), employee seed data (WS8).

## Files owned

`snowman/domain/model/employee.py`, `snowman/domain/repository/{,impl/}employee.py`,
`snowman/domain/service/employee.py`, `snowman/infrastructure/rest/{resources,mappers,routers}/employee.py`,
`tests/**/*employee*`, plus one `include_router` line in `snowman/main.py`.
