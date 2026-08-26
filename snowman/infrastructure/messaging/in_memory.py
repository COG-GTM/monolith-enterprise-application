"""In-memory messaging broker."""

import logging
from collections import defaultdict

from snowman.infrastructure.messaging.broker import Destination, Message

logger = logging.getLogger(__name__)


class InMemoryMessageBroker:
    """Record outbound messages by destination for local operation and tests."""

    def __init__(self) -> None:
        self.messages: dict[str, list[Message]] = defaultdict(list)

    def send(self, destination: Destination, message: Message) -> None:
        self.messages[destination.name].append(message)
        logger.info("Sent message to %s", destination.name)
