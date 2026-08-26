import logging

from snowman.infrastructure.messaging.broker import Destination, Message

logger = logging.getLogger(__name__)


class InMemoryMessageBroker:
    def __init__(self) -> None:
        self.sent: dict[str, list[Message]] = {}

    def send(self, destination: Destination, message: Message) -> None:
        self.sent.setdefault(destination.name, []).append(message)
        logger.info("Sent message to %s", destination.name)
