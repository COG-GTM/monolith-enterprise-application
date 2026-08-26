"""Port of `ClientService.java` / `ClientServiceImpl.java`."""

import json
import logging
from typing import Protocol

from snowman.domain.exception import SnowmanError
from snowman.domain.model.client import Client
from snowman.domain.repository.client import ClientRepository

LOG = logging.getLogger(__name__)

MAX_RETRIES = 3


class ClientSystemGateway(Protocol):
    """Outbound port for the "Client System" REST call (`RestTemplate.getForEntity`)."""

    def fetch_projects(self, client_id: int) -> tuple[int, str]: ...


class ClientService:
    def __init__(self, repository: ClientRepository, client_system: ClientSystemGateway) -> None:
        self._repository = repository
        self._client_system = client_system

    def get_client(self, client_id: int) -> Client | None:
        client = self._repository.get_client(client_id)
        LOG.info("Retrieved client: %s", client)

        if client is None:
            return None

        if not client.projects:
            status, body = self._client_system.fetch_projects(client_id)

            # The Java loop checks `retryCount > MAX_RETRIES` *before* incrementing, so it issues 4
            # retries (5 requests in total) despite MAX_RETRIES = 3.
            retry_count = 0
            while status != 200:
                if retry_count > MAX_RETRIES:
                    break
                status, body = self._client_system.fetch_projects(client_id)
                retry_count += 1

            process_response(body, client)

        return client

    def create_client(self, client: Client) -> None:
        LOG.info("Creating client %s", client)

        if self.get_client(client.id) is not None:
            raise SnowmanError("Client already exists")

        self._repository.create_client(client)

    def update_client(self, client: Client) -> None:
        LOG.info("Updating client %s", client)

        if self.get_client(client.id) is None:
            raise SnowmanError("Client doesn't exists")

        self._repository.update_client(client)

    def delete_client(self, client_id: int) -> None:
        LOG.info("Deleting client with id %s", client_id)

        if self.get_client(client_id) is None:
            LOG.error("Trying to delete a client with id %s that doesn't exist", client_id)
            raise SnowmanError("Client doesn't exists")

        self._repository.delete_client(client_id)


def process_response(body: str, client: Client) -> None:
    # Parity: Java ClientServiceImpl.processResponse discards its result — it reads the "project"
    # node, builds a Set<Project> with one empty Project and never assigns it to the client.
    try:
        root = json.loads(body)
    except ValueError as exc:
        LOG.error("%s", exc)
        return
    project_node = root.get("project") if isinstance(root, dict) else None
    LOG.info("Client System response project node: %s", project_node)
