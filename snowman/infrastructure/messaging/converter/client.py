from snowman.domain.model import Client
from snowman.infrastructure.messaging.converter.project import to_project_dtos
from snowman.infrastructure.messaging.dto import ClientDTO


def to_client_dto(client: Client) -> ClientDTO:
    return ClientDTO(
        clientId=client.id,
        clientName=client.client_name,
        projectDTOS=to_project_dtos(client.projects),
    )
