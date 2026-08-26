import logging

from snowman.infrastructure.messaging.broker import Message, MessageBroker
from snowman.infrastructure.messaging.destinations import PAYROLL_SYSTEM_QUEUE
from snowman.infrastructure.messaging.dto import EmployeeDTO
from snowman.infrastructure.messaging.ports import PayrollSystemPort

logger = logging.getLogger(__name__)


class PayrollSystemAdapter(PayrollSystemPort):
    def __init__(self, broker: MessageBroker) -> None:
        self.broker = broker

    def send_employee_info(self, dto: EmployeeDTO) -> None:
        logger.info("Sending Employee Info %s to external Payroll system", dto)
        self.broker.send(
            PAYROLL_SYSTEM_QUEUE,
            Message(
                payload=dto.model_dump(mode="json"),
                headers={"pristine": True},
                correlation_id=f"EmployeeId-{dto.id}",
                message_id=f"123-0000-{dto.id}",
                priority=1,
                expiration_ms=5000,
                persistent=False,
            ),
        )
