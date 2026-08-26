"""Ports for the Client aggregate — `ClientRepository.java` plus the cache port it needs.

`ClientCache` mirrors WS7's `snowman.infrastructure.cache.ClientCache` protocol structurally; it is
declared here so `domain/` stays free of infrastructure imports. WS7's `TTLClientCache` satisfies it
without changes, so there is only ever one cache implementation.
"""

from typing import Protocol

from snowman.domain.model.client import Client


class ClientRepository(Protocol):
    def get_client(self, client_id: int) -> Client | None: ...

    def create_client(self, client: Client) -> None: ...

    def update_client(self, client: Client) -> None: ...

    def delete_client(self, client_id: int) -> None: ...


class ClientCache(Protocol):
    def get(self, client_id: int) -> Client | None: ...

    def put(self, client_id: int, client: Client) -> None: ...

    def evict(self, client_id: int) -> None: ...

    def clear(self) -> None: ...


class NullClientCache:
    """Null object used until WS7's `clientFindCache` is wired in, and by tests."""

    def get(self, client_id: int) -> Client | None:
        return None

    def put(self, client_id: int, client: Client) -> None:
        return None

    def evict(self, client_id: int) -> None:
        return None

    def clear(self) -> None:
        return None
