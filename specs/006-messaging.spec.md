# WS6 — Messaging layer

**Status:** NOT_STARTED · **Depends on:** WS0

## Purpose

Replace the JMS/ActiveMQ integration with an abstract messaging layer that preserves the outbound
message contracts: destinations, payload shapes and per-message JMS metadata.

## Java source references

* `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/PayrollSystemPort.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/InvoiceSystemPort.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/NotificationPort.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/adapter/{PayrollSystemAdapter,InvoiceSystemAdapter,NotificationAdapter}.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/converter/{EmployeeDTOConverter,ClientDTOConverter,ProjectDTOConverter}.java`
* `src/main/java/com/mycompany/entapp/snowman/infrastructure/messaging/dto/{EmployeeDTO,ClientDTO,ProjectDTO}.java`
* `src/main/resources/META-INF/application-context-messaging.xml` — destinations and templates
* `src/main/resources/application.properties` — `jms.brokerUrl=tcp://localhost:61616`

## Requirements

### R6.1 Destinations (from `application-context-messaging.xml`)

| Bean | Destination | Kind | Used by |
|---|---|---|---|
| `invoiceSystemQueue` | `invoice-system-queue` | queue | `InvoiceSystemAdapter` |
| `payrollSystemQueue` | `payroll-system-queue` | queue | `PayrollSystemAdapter` |
| `notificationTopic` | `notification-topic` | topic | `NotificationAdapter` |

Declare them as module-level constants in `snowman/infrastructure/messaging/destinations.py` using
the WS0 `Destination` type. Do not rename the destination strings.

### R6.2 DTOs — `snowman/infrastructure/messaging/dto/`

Pydantic v2 models mirroring the Java DTO field names (these are the wire contract):

```python
class ProjectDTO(BaseModel):
    projectId: int
    projectTitle: str | None = None
    dateStarted: date | None = None
    dateEnded: date | None = None

class EmployeeDTO(BaseModel):
    id: int
    firstName: str | None = None
    surname: str | None = None
    role: str | None = None
    projectDTOList: list[ProjectDTO] = Field(default_factory=list)

class ClientDTO(BaseModel):
    clientId: int
    clientName: str | None = None
    projectDTOS: list[ProjectDTO] = Field(default_factory=list)
```

`ClientDTO.projectDTOS` is a `Set` in Java, so consumers must not rely on ordering; a list is used
because `ProjectDTO` is not hashable. All three Java DTOs are `Serializable` (they travel as JMS
`ObjectMessage`); the port serializes them as JSON via `model_dump(mode="json")` — record this as
the one intentional wire-format change in the PR description, since a Java `ObjectMessage`
consumer cannot read JSON.

### R6.3 Converters — `snowman/infrastructure/messaging/converter/`

One-way domain → DTO functions only (Java comments state the conversion is one-way):

* `to_project_dto(project)`, `to_project_dtos(projects) -> list[ProjectDTO]`.
* `to_employee_dto(employee)`: `id`, `firstName=employee.firstname`, `surname=employee.surname`,
  `role=employee.role.role`, and `projectDTOList` built from `employee.projects` by walking each
  `EmployeeProject.project` — the port of `EmployeeDTOConverter.getProjectForEmployee`. Emit `None`
  for `role` when the employee has no role rather than raising.
* `to_client_dto(client)`: `clientId`, `clientName`, `projectDTOS=to_project_dtos(client.projects)`.

### R6.4 Adapters — `snowman/infrastructure/messaging/adapters/`

Each adapter takes a `MessageBroker` (WS0) and implements the corresponding WS0 port Protocol.
Per-message metadata must match the Java adapters exactly:

**`PayrollSystemAdapter.send_employee_info(dto: EmployeeDTO)`** → `payroll-system-queue`:

| Java | Message field |
|---|---|
| `setJMSCorrelationID("EmployeeId-" + dto.getId())` | `correlation_id = f"EmployeeId-{dto.id}"` |
| `setBooleanProperty("pristine", true)` | `headers["pristine"] = True` |
| `setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT)` | `persistent = False` |
| `setJMSMessageID("123-0000-" + dto.getId())` | `message_id = f"123-0000-{dto.id}"` |
| `setJMSPriority(1)` | `priority = 1` |
| `setJMSExpiration(5000L)` | `expiration_ms = 5000` |

**`InvoiceSystemAdapter.send_project_info(dto: ClientDTO)`** → `invoice-system-queue` with
`correlation_id = f"ClientID-{dto.clientId}"` (note the differing casing between the two adapters —
`ClientID-` vs `EmployeeId-`; preserve both verbatim) and no other metadata.

**`NotificationAdapter.broadcast_updates(payload: object)`** → `notification-topic`, no metadata
(Java uses bare `convertAndSend`). Payload may be any Pydantic DTO or plain dict; serialize
Pydantic models with `model_dump(mode="json")`, pass other values through unchanged.

Each adapter logs at INFO before sending, mirroring the Java log lines.

### R6.5 Broker selection

`snowman/infrastructure/messaging/factory.py`: `build_broker(settings) -> MessageBroker` returning
`InMemoryMessageBroker` for `memory://` (the default) and raising
`NotImplementedError(f"Unsupported broker URL: {url}")` for anything else, with a docstring naming
STOMP/AMQP as the follow-up integration point. Standing up a real broker is out of scope — the
abstraction is the deliverable.

### R6.6 Wiring

Provide FastAPI dependency factories in `snowman/infrastructure/messaging/deps.py`
(`get_payroll_port`, `get_invoice_port`, `get_notification_port`) over a process-wide broker built
once at startup. **Do not** call the ports from any router or service: nothing in the Java code
does either (the adapters are wired but unused). Note that explicitly in the module docstring so a
reader does not mistake it for an incomplete port.

## Acceptance criteria

1. `tests/infrastructure/messaging/test_adapters.py` asserts, per adapter, the destination, the
   serialized payload and every metadata field in R6.4, using `InMemoryMessageBroker`.
2. `tests/infrastructure/messaging/test_converters.py` asserts the DTO field names and the
   employee→projects walk through `EmployeeProject`, including the role-less employee case.
3. `build_broker` returns the in-memory broker by default and raises for `tcp://localhost:61616`.
4. `ruff check .`, `mypy snowman`, `pytest` pass.
5. No module under `snowman/domain/` imports `snowman.infrastructure.messaging`.

## Out of scope

Inbound message consumption (Java has none), a real broker deployment, and calling the ports from
services (see R6.6).

## Files owned

`snowman/infrastructure/messaging/{destinations,factory,deps}.py`,
`snowman/infrastructure/messaging/{dto,converter,adapters}/**`,
`tests/infrastructure/messaging/**`.
