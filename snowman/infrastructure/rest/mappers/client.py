"""Port of `infrastructure/rest/mappers/ClientResourceMapper.java`."""

from snowman.domain.model.client import Client
from snowman.infrastructure.rest.mappers import project as project_mapper
from snowman.infrastructure.rest.resources.client import ClientResource


def to_resource(client: Client) -> ClientResource:
    return ClientResource(
        clientId=client.id,
        clientName=client.client_name,
        projects=project_mapper.to_resources(client.projects),
    )


def to_client(resource: ClientResource) -> Client:
    # Java's mapToClient passes a possibly-null project list straight to
    # ProjectResourceMapper.mapToProjects and would NPE; a missing list is treated as empty here.
    client = Client()
    client.id = resource.clientId
    client.client_name = resource.clientName
    client.projects = project_mapper.to_projects(resource.projects)
    return client
