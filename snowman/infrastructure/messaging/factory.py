from snowman.config import Settings
from snowman.infrastructure.messaging.broker import MessageBroker
from snowman.infrastructure.messaging.in_memory import InMemoryMessageBroker


def build_broker(settings: Settings) -> MessageBroker:
    """Build a broker; STOMP/AMQP integrations are the follow-up integration point."""
    url = settings.broker_url
    if url == "memory://":
        return InMemoryMessageBroker()
    raise NotImplementedError(f"Unsupported broker URL: {url}")
