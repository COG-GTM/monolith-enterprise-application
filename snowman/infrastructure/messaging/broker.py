from dataclasses import dataclass, field
from typing import Any, Literal, NamedTuple, Protocol


class Destination(NamedTuple):
    name: str
    kind: Literal["queue", "topic"]


@dataclass
class Message:
    payload: Any
    headers: dict[str, Any] = field(default_factory=dict)
    correlation_id: str | None = None
    message_id: str | None = None
    priority: int | None = None
    expiration_ms: int | None = None
    persistent: bool = True


class MessageBroker(Protocol):
    def send(self, destination: Destination, message: Message) -> None:
        ...
