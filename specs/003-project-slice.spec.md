# WS3 — Project slice (incl. EmployeeProject join entities)

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Port the Project vertical slice plus the `employee_project` association mapping that links
employees to projects with start/end dates.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/domain/model/Project.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/model/EmployeeProject.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/model/EmployeeProjectV2.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/model/EmployeeProjectId.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/service/ProjectService.java` + `impl/ProjectServiceImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/domain/repository/ProjectRepository.java` + `impl/ProjectRepositoryImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/db/dao/ProjectDao.java` + `impl/ProjectDaoImpl.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/endpoint/ProjectRestEndpoint.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/resources/ProjectResource.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/rest/mappers/ProjectResourceMapper.java`
* Tests to mirror: `.../domain/service/impl/ProjectServiceImplUTest.java`,
  `.../domain/repository/impl/ProjectRepositoryImplUTest.java`,
  `.../infrastructure/rest/endpoint/ProjectRestEndpointUTest.java`,
  `.../infrastructure/rest/mappers/ProjectResourceMapperUTest.java`

## Requirements

### R3.1 Models (refine the WS0 stubs)

`snowman/domain/model/project.py`:

```python
class Project(Base):
    __tablename__ = "project"
    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    project_title: Mapped[str] = mapped_column("project_title", String(20), nullable=False)
    date_started: Mapped[date] = mapped_column("date_started", Date, nullable=False)
    date_ended: Mapped[date | None] = mapped_column("date_ended", Date)
    client_id: Mapped[int] = mapped_column(ForeignKey("client.id", ondelete="CASCADE"), nullable=False)
    client: Mapped["Client"] = relationship(back_populates="projects")
    employee_projects: Mapped[list["EmployeeProject"]] = relationship(back_populates="project", lazy="select")
```

`snowman/domain/model/employee_project.py` — single mapping of the `employee_project` table with a
**composite primary key** `(employee_id, project_id)`, which is what all three Java classes
(`EmployeeProject`, `EmployeeProjectV2` + `EmployeeProjectId`) express in different Hibernate
dialects:

```python
class EmployeeProject(Base):
    __tablename__ = "employee_project"
    employee_id: Mapped[int] = mapped_column(ForeignKey("employee.id"), primary_key=True)
    project_id: Mapped[int] = mapped_column(ForeignKey("project.id"), primary_key=True)
    date_started: Mapped[date | None] = mapped_column("date_started", Date)
    date_ended: Mapped[date | None] = mapped_column("date_ended", Date)
    employee: Mapped["Employee"] = relationship(back_populates="projects")
    project: Mapped["Project"] = relationship(back_populates="employee_projects")
```

Python has no reason to carry two mappings of one table: `EmployeeProjectV2`/`EmployeeProjectId` are
**not** ported as separate classes. Record that in a module docstring citing all three Java files.
Note the Liquibase DDL declares no foreign keys on `employee_project`; the ORM-level `ForeignKey`s
are additive and must match the FK-free DDL WS8 writes (i.e. do not let autogenerate add
constraints to the migration).

### R3.2 Resource — `snowman/infrastructure/rest/resources/project.py`

```python
class ProjectResource(BaseModel):
    projectId: int = 0
    title: str
    dateStarted: date | None = None
    dateEnded: date | None = None
```

Field names as serialized by Java (`title`, not `projectTitle`). `java.util.Date` serializes to a
date; use `datetime.date`. `ClientResource` (WS2) imports this model.

### R3.3 Mapper — `snowman/infrastructure/rest/mappers/project.py`

* `to_resource(project) -> ProjectResource`, `to_project(resource) -> Project`. Java's `mapToProject`
  deliberately does **not** set the client (`//project.setClient`); the port also leaves `client`
  unset — keep the comment explaining that the join is not part of the payload.
* `to_projects(resources: Iterable[ProjectResource]) -> list[Project]` and
  `to_resources(projects: Iterable[Project]) -> list[ProjectResource]` (Java uses `Set`/`List`;
  Python uses lists, since `Project` is unhashable by value).

### R3.4 Repository — `snowman/domain/repository/{,impl/}project.py`

Protocol: `find_project(project_id) -> Project | None`, `save_project(project) -> None`,
`remove_project(project_id) -> None`. Implementation: `session.get`, `session.merge` + flush, and
load-then-delete (Java `ProjectDaoImpl.removeProject` deletes a bare int — a bug; delete the entity).

### R3.5 Service — `snowman/domain/service/project.py`

| Method | Java behavior to preserve |
|---|---|
| `get_project(project_id)` | delegate |
| `create_project(project)` | unconditional save |
| `update_project(project)` | if not found → `EntityNotFoundError(f"Can't update an unknown project {project}")` |
| `delete_project(project_id)` | if not found → `EntityNotFoundError(f"Can't remove an unknown project with id: {project_id}")` |

### R3.6 Router — `snowman/infrastructure/rest/routers/project.py`

`APIRouter(prefix="/project", tags=["project"])`:

| Method | Path | Request | Response |
|---|---|---|---|
| GET | `/project/{projectId}` | — | `200` `ProjectResource`; `404` if unknown |
| POST | `/project/create` | `ProjectResource` JSON | `200`, empty body |
| POST | `/project/update` | `ProjectResource` JSON | `200`, empty body; `404` if unknown |
| POST | `/project/update}` | `ProjectResource` JSON | same handler, `include_in_schema=False` |
| DELETE | `/project/{projectId}/delete` | — | `200`, empty body; `404` if unknown |

The `update}` alias reproduces the stray brace in the Java mapping (see deviation 2 in `plan.md`);
`/project/update` is the canonical route. Java's endpoints accept every verb because
`@RequestMapping` omits `method`; the port pins the verbs above (deviation 3).

## Acceptance criteria

1. Service tests cover all four methods and both error messages verbatim.
2. Router tests exercise every row of the table above, including `POST /project/update}` returning
   200 and being absent from `/openapi.json`.
3. A model test persists an `Employee`, `Project` and `EmployeeProject`, then asserts
   `employee.projects[0].project is project` and `project.employee_projects[0].employee is employee`.
4. `ruff check .`, `mypy snowman`, `pytest` pass.

## Out of scope

Client model/service (WS2), employee model (WS1), project seed data (WS8), invoice messaging (WS6).

## Files owned

`snowman/domain/model/{project,employee_project}.py`,
`snowman/domain/repository/{,impl/}project.py`, `snowman/domain/service/project.py`,
`snowman/infrastructure/rest/{resources,mappers,routers}/project.py`, `tests/**/*project*`,
plus one `include_router` line in `snowman/main.py`.
