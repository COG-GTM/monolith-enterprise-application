import httpx
import pytest

from snowman.infrastructure.rest import client_system
from snowman.infrastructure.rest.client_system import HttpxClientSystemGateway


def test_fetch_projects_substitutes_the_client_id(monkeypatch: pytest.MonkeyPatch) -> None:
    called: dict[str, str] = {}

    def _fake_get(url: str, timeout: float) -> httpx.Response:
        called["url"] = url
        return httpx.Response(200, text='{"project": []}')

    monkeypatch.setattr(client_system.httpx, "get", _fake_get)
    gateway = HttpxClientSystemGateway(url_template="http://cs/client/{clientId}/projects")

    assert gateway.fetch_projects(42) == (200, '{"project": []}')
    assert called["url"] == "http://cs/client/42/projects"


def test_fetch_projects_maps_transport_errors_to_503(monkeypatch: pytest.MonkeyPatch) -> None:
    def _raise(url: str, timeout: float) -> httpx.Response:
        raise httpx.ConnectError("boom")

    monkeypatch.setattr(client_system.httpx, "get", _raise)
    gateway = HttpxClientSystemGateway(url_template="http://cs/client/{clientId}/projects")

    assert gateway.fetch_projects(1) == (503, "")
