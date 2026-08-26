"""Messaging port types."""

from dataclasses import dataclass, field
from typing import Any, Literal, NamedTuple, Protocol


class Destination(NamedTuple):
    """A named queue or topic."""

    name: str
    kind: Literal["queue", "topic"]


@dataclass
class Message:
    """A broker message and its JMS-compatible metadata."""

    payload: Any
    headers: dict[str, Any] = field(default_factory=dict)
    correlation_id: str | None = None
    message_id: str | None = None
    priority: int | None = None
    expiration_ms: int | None = None
    persistent: bool = True


class MessageBroker(Protocol):
    """Outbound messaging broker abstraction."""

    def send(self, destination: Destination, message: Message) -> None:
        """Send a message to a destination."""
