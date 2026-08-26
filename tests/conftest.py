import pytest

from snowman.infrastructure.messaging.in_memory import InMemoryMessageBroker


@pytest.fixture
def broker() -> InMemoryMessageBroker:
    return InMemoryMessageBroker()
