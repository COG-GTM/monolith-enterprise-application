import logging
from typing import Any

from pydantic import BaseModel

from snowman.infrastructure.messaging.broker import Message, MessageBroker
from snowman.infrastructure.messaging.destinations import NOTIFICATION_TOPIC
from snowman.infrastructure.messaging.ports import NotificationPort

logger = logging.getLogger(__name__)


class NotificationAdapter(NotificationPort):
    def __init__(self, broker: MessageBroker) -> None:
        self.broker = broker

    def broadcast_updates(self, payload: object) -> None:
        logger.info("Sending object %s as update notification", payload)
        serialized: Any = (
            payload.model_dump(mode="json") if isinstance(payload, BaseModel) else payload
        )
        self.broker.send(NOTIFICATION_TOPIC, Message(payload=serialized))
