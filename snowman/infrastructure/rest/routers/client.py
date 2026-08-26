"""Port of `infrastructure/rest/endpoint/ClientRestEndpoint.java`."""

from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from snowman.db.session import get_db
from snowman.domain.repository.client import ClientCache, NullClientCache
from snowman.domain.repository.impl.client import SqlAlchemyClientRepository
from snowman.domain.service.client import ClientService, ClientSystemGateway
from snowman.infrastructure.rest.client_system import HttpxClientSystemGateway
from snowman.infrastructure.rest.mappers import client as client_mapper
from snowman.infrastructure.rest.resources.client import ClientResource

router = APIRouter(prefix="/client", tags=["client"])

_NULL_CACHE = NullClientCache()


def get_client_cache() -> ClientCache:
    # Overridden with WS7's shared `clientFindCache` instance once it lands.
    return _NULL_CACHE


def get_client_system() -> ClientSystemGateway:
    return HttpxClientSystemGateway()


def get_client_service(
    session: Annotated[Session, Depends(get_db)],
    cache: Annotated[ClientCache, Depends(get_client_cache)],
    client_system: Annotated[ClientSystemGateway, Depends(get_client_system)],
) -> ClientService:
    return ClientService(SqlAlchemyClientRepository(session, cache), client_system)


ServiceDep = Annotated[ClientService, Depends(get_client_service)]


@router.get("/{clientId}", response_model=ClientResource)
def get_client_info(clientId: int, service: ServiceDep) -> ClientResource:
    client = service.get_client(clientId)
    if client is None:
        raise HTTPException(status_code=404, detail=f"No client with id: {clientId}")
    return client_mapper.to_resource(client)


@router.post("/new")
def create_client_info(resource: ClientResource, service: ServiceDep) -> None:
    service.create_client(client_mapper.to_client(resource))


@router.post("/update")
def update_client_info(resource: ClientResource, service: ServiceDep) -> None:
    service.update_client(client_mapper.to_client(resource))


@router.delete("/{clientId}")
def delete_client_info(clientId: int, service: ServiceDep) -> None:
    service.delete_client(clientId)
