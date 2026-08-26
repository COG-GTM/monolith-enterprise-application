"""httpx adapter for the "Client System" REST call — port of the Spring `RestTemplate` usage."""

import logging

import httpx

from snowman.config import get_settings

LOG = logging.getLogger(__name__)


class HttpxClientSystemGateway:
    def __init__(self, url_template: str | None = None, timeout: float = 5.0) -> None:
        self._url_template = url_template or get_settings().client_system_url
        self._timeout = timeout

    def fetch_projects(self, client_id: int) -> tuple[int, str]:
        url = self._url_template.replace("{clientId}", str(client_id))
        try:
            response = httpx.get(url, timeout=self._timeout)
        except httpx.HTTPError as exc:
            LOG.error("Client System request to %s failed: %s", url, exc)
            return 503, ""
        return response.status_code, response.text
