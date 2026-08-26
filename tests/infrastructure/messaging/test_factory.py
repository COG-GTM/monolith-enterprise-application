import pytest

from snowman.config import Settings
from snowman.infrastructure.messaging.factory import build_broker
from snowman.infrastructure.messaging.in_memory import InMemoryMessageBroker


def test_build_broker_defaults_to_in_memory() -> None:
    assert isinstance(build_broker(Settings()), InMemoryMessageBroker)


def test_build_broker_rejects_unsupported_url() -> None:
    settings = Settings(broker_url="tcp://localhost:61616")

    with pytest.raises(
        NotImplementedError,
        match=r"^Unsupported broker URL: tcp://localhost:61616$",
    ):
        build_broker(settings)
