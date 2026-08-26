"""Test doubles for the Client slice (WS2)."""

from snowman.domain.model.client import Client


class FakeClientRepository:
    def __init__(self, clients: dict[int, Client]) -> None:
        self.clients = clients
        self.created: list[Client] = []
        self.updated: list[Client] = []
        self.deleted: list[int] = []

    def get_client(self, client_id: int) -> Client | None:
        return self.clients.get(client_id)

    def create_client(self, client: Client) -> None:
        self.created.append(client)
        self.clients[client.id] = client

    def update_client(self, client: Client) -> None:
        self.updated.append(client)
        self.clients[client.id] = client

    def delete_client(self, client_id: int) -> None:
        self.deleted.append(client_id)
        self.clients.pop(client_id, None)


class RecordingClientSystemGateway:
    """Returns the queued responses in order, repeating the last one once exhausted."""

    def __init__(self, responses: list[tuple[int, str]]) -> None:
        self.responses = responses
        self.calls = 0

    def fetch_projects(self, client_id: int) -> tuple[int, str]:
        index = min(self.calls, len(self.responses) - 1)
        self.calls += 1
        return self.responses[index]


class FakeClientCache:
    """Null-object cache that records every operation (stands in for WS7's clientFindCache)."""

    def __init__(self) -> None:
        self.entries: dict[int, Client] = {}
        self.operations: list[tuple[str, int]] = []

    def get(self, client_id: int) -> Client | None:
        self.operations.append(("get", client_id))
        return self.entries.get(client_id)

    def put(self, client_id: int, client: Client) -> None:
        self.operations.append(("put", client_id))
        self.entries[client_id] = client

    def evict(self, client_id: int) -> None:
        self.operations.append(("evict", client_id))
        self.entries.pop(client_id, None)

    def clear(self) -> None:
        self.entries.clear()
