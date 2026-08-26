"""Port of `ClientRepositoryImpl.java` + `ClientDaoImpl.java`.

The Spring cache annotations on `clientFindCache` become explicit cache-aside calls:

| Java annotation | Port behavior |
|---|---|
| `@Cacheable(key="#clientId")` on `getClient` | serve from cache, else load and store |
| `@CachePut(key="#client.clientId")` on `updateClient` | save, then store unconditionally |
| `@CacheEvict(key="#clientId")` on `deleteClient` | delete, then evict the key |
| (none) on `createClient` | no cache interaction |
"""

from sqlalchemy.orm import Session

from snowman.domain.model.client import Client
from snowman.domain.repository.client import ClientCache


class SqlAlchemyClientRepository:
    def __init__(self, session: Session, cache: ClientCache) -> None:
        self._session = session
        self._cache = cache

    def get_client(self, client_id: int) -> Client | None:
        cached = self._cache.get(client_id)
        if cached is not None:
            return cached

        client = self._session.get(Client, client_id)
        if client is not None:
            self._cache.put(client_id, client)
        return client

    def create_client(self, client: Client) -> None:
        self._session.add(client)
        self._session.flush()

    def update_client(self, client: Client) -> None:
        # `Client.projects` is the inverse side of the Hibernate mapping (`mappedBy = "client"`), so
        # saving a client never re-parents project rows: only the scalar columns are written.
        existing = self._session.get(Client, client.id)
        target = client if existing is None else existing
        if existing is None:
            self._session.add(client)
        else:
            existing.client_name = client.client_name
        self._session.flush()
        self._cache.put(target.id, target)

    def delete_client(self, client_id: int) -> None:
        client = self._session.get(Client, client_id)
        if client is not None:
            self._session.delete(client)
            self._session.flush()
        self._cache.evict(client_id)
