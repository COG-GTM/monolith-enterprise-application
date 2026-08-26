from datetime import date

from snowman.infrastructure.messaging.adapters import (
    InvoiceSystemAdapter,
    NotificationAdapter,
    PayrollSystemAdapter,
)
from snowman.infrastructure.messaging.destinations import (
    INVOICE_SYSTEM_QUEUE,
    NOTIFICATION_TOPIC,
    PAYROLL_SYSTEM_QUEUE,
)
from snowman.infrastructure.messaging.dto import ClientDTO, EmployeeDTO, ProjectDTO


def test_payroll_adapter_sends_payload_and_metadata(broker) -> None:
    dto = EmployeeDTO(
        id=7,
        firstName="Ada",
        surname="Lovelace",
        role="Engineer",
        projectDTOList=[ProjectDTO(projectId=3, projectTitle="Engine")],
    )

    PayrollSystemAdapter(broker).send_employee_info(dto)

    message = broker.sent[PAYROLL_SYSTEM_QUEUE.name][0]
    assert message.payload == dto.model_dump(mode="json")
    assert message.headers == {"pristine": True}
    assert message.correlation_id == "EmployeeId-7"
    assert message.message_id == "123-0000-7"
    assert message.priority == 1
    assert message.expiration_ms == 5000
    assert message.persistent is False


def test_invoice_adapter_sends_payload_and_correlation_id(broker) -> None:
    dto = ClientDTO(
        clientId=11,
        clientName="Acme",
        projectDTOS=[ProjectDTO(projectId=3, dateStarted=date(2024, 1, 2))],
    )

    InvoiceSystemAdapter(broker).send_project_info(dto)

    message = broker.sent[INVOICE_SYSTEM_QUEUE.name][0]
    assert message.payload == dto.model_dump(mode="json")
    assert message.correlation_id == "ClientID-11"
    assert message.headers == {}
    assert message.message_id is None
    assert message.priority is None
    assert message.expiration_ms is None
    assert message.persistent is True


def test_notification_adapter_serializes_pydantic_payload(broker) -> None:
    dto = ProjectDTO(projectId=3, dateStarted=date(2024, 1, 2))

    NotificationAdapter(broker).broadcast_updates(dto)

    message = broker.sent[NOTIFICATION_TOPIC.name][0]
    assert message.payload == dto.model_dump(mode="json")
    assert message.headers == {}
    assert message.correlation_id is None
    assert message.message_id is None
    assert message.priority is None
    assert message.expiration_ms is None
    assert message.persistent is True


def test_notification_adapter_passes_plain_payload_through(broker) -> None:
    payload = {"status": "updated"}

    NotificationAdapter(broker).broadcast_updates(payload)

    assert broker.sent[NOTIFICATION_TOPIC.name][0].payload is payload
