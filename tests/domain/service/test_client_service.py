"""Mirrors `ClientServiceImplUTest.java`."""

import pytest

from snowman.domain.exception import SnowmanError
from snowman.domain.model.client import Client
from snowman.domain.model.project import Project
from snowman.domain.service.client import ClientService, process_response
from tests.client_doubles import FakeClientRepository, RecordingClientSystemGateway


def _client(client_id: int = 1, projects: list[Project] | None = None) -> Client:
    client = Client()
    client.id = client_id
    client.client_name = "Test Client"
    client.projects = projects or []
    return client


def _project() -> Project:
    project = Project()
    project.id = 10
    project.project_title = "Snowman"
    return project


def test_get_client_with_projects_never_calls_client_system() -> None:
    client = _client(projects=[_project()])
    gateway = RecordingClientSystemGateway(responses=[(200, "{}")])
    service = ClientService(FakeClientRepository({1: client}), gateway)

    assert service.get_client(1) is client
    assert gateway.calls == 0


def test_get_client_unknown_id_returns_none() -> None:
    gateway = RecordingClientSystemGateway(responses=[(200, "{}")])
    service = ClientService(FakeClientRepository({}), gateway)

    assert service.get_client(99) is None
    assert gateway.calls == 0


def test_get_client_without_projects_calls_client_system_once_on_success() -> None:
    gateway = RecordingClientSystemGateway(responses=[(200, '{"project": {"id": 1}}')])
    service = ClientService(FakeClientRepository({1: _client()}), gateway)

    service.get_client(1)

    assert gateway.calls == 1


def test_retry_bound_is_five_requests_when_never_ok() -> None:
    # MAX_RETRIES = 3 but the Java loop breaks only once retryCount > MAX_RETRIES: 1 + 4 requests.
    gateway = RecordingClientSystemGateway(responses=[(500, "{}")])
    service = ClientService(FakeClientRepository({1: _client()}), gateway)

    service.get_client(1)

    assert gateway.calls == 5


def test_process_response_does_not_mutate_the_client() -> None:
    client = _client()
    process_response('{"project": [{"id": 1, "title": "x"}]}', client)
    assert client.projects == []


def test_process_response_swallows_invalid_json() -> None:
    client = _client()
    process_response("not json", client)
    assert client.projects == []


def test_create_client_raises_when_client_exists() -> None:
    repository = FakeClientRepository({1: _client(projects=[_project()])})
    service = ClientService(repository, RecordingClientSystemGateway(responses=[(200, "{}")]))

    with pytest.raises(SnowmanError, match="^Client already exists$"):
        service.create_client(_client())

    assert repository.created == []


def test_create_client_saves_when_absent() -> None:
    repository = FakeClientRepository({})
    service = ClientService(repository, RecordingClientSystemGateway(responses=[(200, "{}")]))
    client = _client(client_id=2)

    service.create_client(client)

    assert repository.created == [client]


def test_update_client_raises_when_absent() -> None:
    repository = FakeClientRepository({})
    service = ClientService(repository, RecordingClientSystemGateway(responses=[(200, "{}")]))

    with pytest.raises(SnowmanError, match="^Client doesn't exists$"):
        service.update_client(_client())

    assert repository.updated == []


def test_update_client_saves_when_present() -> None:
    existing = _client(projects=[_project()])
    repository = FakeClientRepository({1: existing})
    service = ClientService(repository, RecordingClientSystemGateway(responses=[(200, "{}")]))

    service.update_client(existing)

    assert repository.updated == [existing]


def test_delete_client_raises_when_absent() -> None:
    repository = FakeClientRepository({})
    service = ClientService(repository, RecordingClientSystemGateway(responses=[(200, "{}")]))

    with pytest.raises(SnowmanError, match="^Client doesn't exists$"):
        service.delete_client(1)

    assert repository.deleted == []


def test_delete_client_removes_when_present() -> None:
    repository = FakeClientRepository({1: _client(projects=[_project()])})
    service = ClientService(repository, RecordingClientSystemGateway(responses=[(200, "{}")]))

    service.delete_client(1)

    assert repository.deleted == [1]
