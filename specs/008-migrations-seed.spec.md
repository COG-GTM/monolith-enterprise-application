# WS8 — Alembic migrations + seed data

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Translate the four Liquibase changelogs into four Alembic revisions — schema first, then the three
data-loading changelogs — so a fresh database reaches exactly the state the Java app expects.

## Java source references

* `src/main/resources/db/liquibase-changelog-schema.xml` — the master changelog (include order)
* `src/main/resources/db/changelog/001_Create_Schema.xml` — 3 changesets: domain tables, `user`, `app_info`
* `src/main/resources/db/changelog/002_Insert_Initial_Data.xml` — 5 changesets: roles, employees, clients, projects, employee_project
* `src/main/resources/db/changelog/003_Insert_App_Info.xml` — 1 changeset: `app_info` row
* `src/main/resources/db/changelog/004_Insert_Dummy_Users.xml` — 1 changeset: 4 users
* `src/main/resources/db/scripts/{init-db.sql,reset-db.sql}`
* `src/main/resources/db/liquibase.properties` — target DB `snowman`

## Requirements

### R8.1 Revision chain

Four revisions, in this order, each with a working `downgrade()` mirroring the Liquibase
`<rollback>` blocks:

| Revision slug | Ports | Downgrade |
|---|---|---|
| `0001_create_schema` | `001_Create_Schema.xml` (all 3 changesets) | drop `employee_project`, `project`, `client`, `employee`, `employee_role`, `user`, `app_info` |
| `0002_insert_initial_data` | `002_Insert_Initial_Data.xml` | delete all rows from `employee_project`, `project`, `client`, `employee`, `employee_role` |
| `0003_insert_app_info` | `003_Insert_App_Info.xml` | delete all rows from `app_info` |
| `0004_insert_dummy_users` | `004_Insert_Dummy_Users.xml` | delete all rows from `user` |

Use `op.create_table` / `op.bulk_insert` (not raw multi-statement SQL strings) so the revisions run
on SQLite and MySQL alike. Liquibase's rollbacks use `truncate`; `DELETE FROM` is the portable
equivalent (SQLite has no `TRUNCATE`) — note that in each revision's docstring. Note also that
changesets 4 and 5 of `002` both declare `truncate table project` in their rollback — a copy/paste
bug in the source; the port deletes from `employee_project` in the `0002` downgrade before deleting
projects, which is what the intent requires.

### R8.2 Schema (`0001`), exactly as in `001_Create_Schema.xml`

```sql
employee_role(id INT PRIMARY KEY, role VARCHAR(30) NOT NULL)
employee(id INT PRIMARY KEY AUTO_INCREMENT, firstname VARCHAR(20) NOT NULL,
         surname VARCHAR(20) NOT NULL, employee_role_id INT,
         FOREIGN KEY (employee_role_id) REFERENCES employee_role(id) ON DELETE CASCADE)
client(id INT PRIMARY KEY, client_name VARCHAR(30) NOT NULL)
project(id INT PRIMARY KEY AUTO_INCREMENT, project_title VARCHAR(20) NOT NULL,
        date_started DATE NOT NULL, date_ended DATE, client_id INT NOT NULL,
        FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE)
employee_project(employee_id INT, project_id INT, date_started DATE, date_ended DATE,
                 PRIMARY KEY (employee_id, project_id))          -- no FKs in the source DDL
user(id INT, username VARCHAR(20), password VARCHAR(20), email VARCHAR(20),
     firstname VARCHAR(20), secondname VARCHAR(20))
app_info(id INT, version VARCHAR(20))
```

Rules:
* `employee.id` and `project.id` are autoincrement; `employee_role.id` and `client.id` are **not**.
* `employee_project` gets **no** foreign keys (matching the source DDL) but keeps the composite PK.
* `user.id` and `app_info.id` become primary keys (deviation 5 in `plan.md`); all their other
  columns stay nullable. Do not add any column the source lacks.
* Column names are lowercase exactly as above — in particular `user.secondname`.

### R8.3 Seed data (`0002`), byte-for-byte from `002_Insert_Initial_Data.xml`

* **32 employee roles**, ids 1–32 in source order: `Development Manager`, `Testing Manager`,
  `Software Developer`, `Technical Architect`, `Solutions Architect`, `Enterprise Architect`,
  `Data Architect`, `Integration Architect`, `Systems Architect`, `Infrastructure Architect`,
  `Operations Architect`, `Frontend Architect`, `Build Engineer`, `Java Developer`,
  `Full Stack Developer`, `Frontend Developer`, `Team Lead`, `Operations Engineer`,
  `Systems Administrator`, `Linux Engineer`, `DevOps Engineer`, `Database Administrator`,
  `Test Engineer`, `QA`, `Test Automation Engineer`, `SDET`, `Developer In Test`, `Tech Tester`,
  `Business Analyst`, `Product Owner`, `Scrum Master`, `Support Analyst`.
* **4 employees** (ids auto-assigned 1–4 in insert order):
  `('Colin','But',3)`, `('PersonA','SurnameA',28)`, `('Firstname','Secondname',24)`, `('Danny','Little',18)`.
* **6 clients**: `(1,'client x')`, `(2,'client y')`, `(3,'client z')`, `(4,'client a')`,
  `(5,'client b')`, `(6,'client c')`.
* **5 projects** (ids auto-assigned 1–5 in insert order):
  `('project 1','2017-03-15',NULL,1)`, `('government project 1','2016-02-15',NULL,2)`,
  `('financial project','2011-08-15','2014-09-03',3)`, `('e-commerce project','2015-12-15',NULL,4)`,
  `('Project X','2017-06-15',NULL,5)`.
* **5 employee_project rows** `(employee_id, project_id, date_started, date_ended)`:
  `(1,3,'2011-08-31','2013-06-15')`, `(2,2,'2017-06-15',NULL)`, `(3,3,'2017-06-15',NULL)`,
  `(4,4,'2017-06-15',NULL)`, `(1,5,'2017-06-15',NULL)`.

Employees and projects rely on autoincrement ordering for these FK references, so insert them in the
listed order and pass explicit ids in the bulk insert to make the association rows deterministic.

### R8.4 App info (`0003`) and users (`0004`)

* `app_info`: `(id=1, version='1.0.0')`.
* `user` (`id, username, password, email, firstname, secondname`):
  `(1,'username','password','username@email.com','user first name','user second name')`,
  `(2,'admin','admin','admin@email.com','admin first name','admin second name')`,
  `(3,'test','test','test@email.com','test first name','test second name')`,
  `(4,'dev','dev','dev@email.com','dev first name','dev second name')`.

### R8.5 Model/migration consistency check

Add `tests/test_migrations.py` that runs `alembic upgrade head` against a temporary SQLite database
and asserts:
1. every table in `Base.metadata.tables` exists with the same column names;
2. `alembic revision --autogenerate` on the upgraded DB produces an **empty** diff (compare via
   `alembic.autogenerate.compare_metadata`), ignoring FK differences on `employee_project` documented
   in R8.2;
3. row counts: `employee_role` 32, `employee` 4, `client` 6, `project` 5, `employee_project` 5,
   `app_info` 1, `user` 4;
4. `alembic downgrade base` then `upgrade head` succeeds (idempotent chain).

### R8.6 Developer docs

Add a `## Database` section to the new `snowman` README (or `specs/008` addendum) covering
`alembic upgrade head`, `alembic downgrade -1`, and how to point `SNOWMAN_DATABASE_URL` at MySQL
(`mysql+pymysql://username:password@localhost:3306/snowman`) — the credentials in
`db/liquibase.properties`.

## Acceptance criteria

1. `alembic upgrade head` succeeds on a fresh SQLite DB and on MySQL 8 (SQLite is the CI gate).
2. `tests/test_migrations.py` passes, including the empty-autogenerate-diff assertion.
3. `alembic downgrade base` removes every table created here.
4. `ruff check .`, `mypy snowman`, `pytest` pass.

## Out of scope

Liquibase → Alembic changelog *tracking* history (the Alembic chain starts fresh; no attempt to
read `DATABASECHANGELOG`), MySQL-specific tuning, and `db/scripts/*.sql` (developer conveniences).

## Files owned

`alembic/versions/**`, `tests/test_migrations.py`, plus the `## Database` docs section.
