import logging

from snowman.infrastructure.messaging.broker import Message, MessageBroker
from snowman.infrastructure.messaging.destinations import INVOICE_SYSTEM_QUEUE
from snowman.infrastructure.messaging.dto import ClientDTO
from snowman.infrastructure.messaging.ports import InvoiceSystemPort

logger = logging.getLogger(__name__)


class InvoiceSystemAdapter(InvoiceSystemPort):
    def __init__(self, broker: MessageBroker) -> None:
        self.broker = broker

    def send_project_info(self, dto: ClientDTO) -> None:
        logger.info("Sending client info to Invoice System: %s", dto)
        self.broker.send(
            INVOICE_SYSTEM_QUEUE,
            Message(
                payload=dto.model_dump(mode="json"),
                correlation_id=f"ClientID-{dto.clientId}",
            ),
        )
