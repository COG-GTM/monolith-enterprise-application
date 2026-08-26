"""Mirrors `ClientResourceMapperUTest.java`."""

from datetime import date

from snowman.domain.model.client import Client
from snowman.domain.model.project import Project
from snowman.infrastructure.rest.mappers import client as client_mapper
from snowman.infrastructure.rest.resources.client import ClientResource
from snowman.infrastructure.rest.resources.project import ProjectResource


def test_to_resource_maps_every_field() -> None:
    project = Project()
    project.id = 5
    project.project_title = "Snowman"
    project.date_started = date(2018, 1, 1)
    project.date_ended = None
    client = Client()
    client.id = 1
    client.client_name = "Acme"
    client.projects = [project]

    resource = client_mapper.to_resource(client)

    assert resource.clientId == 1
    assert resource.clientName == "Acme"
    assert [p.projectId for p in resource.projects] == [5]
    assert resource.projects[0].title == "Snowman"
    assert resource.projects[0].dateStarted == date(2018, 1, 1)


def test_to_client_maps_every_field() -> None:
    resource = ClientResource(
        clientId=2,
        clientName="Globex",
        projects=[ProjectResource(projectId=9, title="Port", dateStarted=date(2020, 2, 2))],
    )

    client = client_mapper.to_client(resource)

    assert client.id == 2
    assert client.client_name == "Globex"
    assert [p.id for p in client.projects] == [9]


def test_to_client_treats_missing_projects_as_empty() -> None:
    client = client_mapper.to_client(ClientResource(clientId=3, clientName="Initech"))

    assert client.projects == []
