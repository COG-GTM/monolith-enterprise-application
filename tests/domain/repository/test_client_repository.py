"""Cache-aside behavior ported from the Spring cache annotations on ClientRepositoryImpl."""

from sqlalchemy.orm import Session

from snowman.domain.model.client import Client
from snowman.domain.model.project import Project
from snowman.domain.repository.impl.client import SqlAlchemyClientRepository
from snowman.infrastructure.cache.client_cache import TTLClientCache
from tests.client_doubles import FakeClientCache


def _persist_client(session: Session, client_id: int = 1) -> Client:
    client = Client()
    client.id = client_id
    client.client_name = "Acme"
    session.add(client)
    session.flush()
    return client


def test_get_client_loads_and_stores_on_miss(db_session: Session) -> None:
    _persist_client(db_session)
    cache = FakeClientCache()
    repository = SqlAlchemyClientRepository(db_session, cache)

    client = repository.get_client(1)

    assert client is not None
    assert cache.operations == [("get", 1), ("put", 1)]


def test_get_client_serves_from_cache_without_touching_the_session(db_session: Session) -> None:
    cache = FakeClientCache()
    cached = Client()
    cached.id = 7
    cached.client_name = "Cached"
    cache.entries[7] = cached
    repository = SqlAlchemyClientRepository(db_session, cache)

    assert repository.get_client(7) is cached
    assert cache.operations == [("get", 7)]


def test_get_client_unknown_id_returns_none_and_caches_nothing(db_session: Session) -> None:
    cache = FakeClientCache()
    repository = SqlAlchemyClientRepository(db_session, cache)

    assert repository.get_client(404) is None
    assert cache.operations == [("get", 404)]


def test_create_client_does_not_touch_the_cache(db_session: Session) -> None:
    cache = FakeClientCache()
    repository = SqlAlchemyClientRepository(db_session, cache)
    client = Client()
    client.id = 2
    client.client_name = "New"

    repository.create_client(client)

    assert db_session.get(Client, 2) is not None
    assert cache.operations == []


def test_update_client_puts_the_client_in_the_cache(db_session: Session) -> None:
    _persist_client(db_session, client_id=3)
    cache = FakeClientCache()
    repository = SqlAlchemyClientRepository(db_session, cache)
    updated = Client()
    updated.id = 3
    updated.client_name = "Renamed"

    repository.update_client(updated)

    assert cache.operations == [("put", 3)]
    assert cache.entries[3].client_name == "Renamed"


def test_delete_client_evicts_the_key(db_session: Session) -> None:
    _persist_client(db_session, client_id=4)
    cache = FakeClientCache()
    cache.entries[4] = Client()
    repository = SqlAlchemyClientRepository(db_session, cache)

    repository.delete_client(4)

    assert db_session.get(Client, 4) is None
    assert cache.operations == [("evict", 4)]


def test_delete_client_cascades_to_its_projects(db_session: Session) -> None:
    from datetime import date

    _persist_client(db_session, client_id=5)
    project = Project()
    project.project_title = "Snowman"
    project.date_started = date(2018, 1, 1)
    project.client_id = 5
    db_session.add(project)
    db_session.flush()
    repository = SqlAlchemyClientRepository(db_session, FakeClientCache())

    repository.delete_client(5)

    assert db_session.get(Client, 5) is None
    assert db_session.query(Project).filter_by(client_id=5).count() == 0


def test_real_cache_survives_session_close_after_update(db_engine) -> None:
    from datetime import date

    setup_session = Session(db_engine)
    client = Client(id=903, client_name="C3")
    project = Project(
        project_title="Project",
        date_started=date(2024, 1, 1),
        client_id=903,
    )
    setup_session.add_all([client, project])
    setup_session.commit()
    setup_session.close()

    cache = TTLClientCache()
    load_session = Session(db_engine)
    loaded = SqlAlchemyClientRepository(load_session, cache).get_client(903)
    assert loaded is not None
    assert [item.project_title for item in loaded.projects] == ["Project"]
    load_session.close()

    update_session = Session(db_engine)
    SqlAlchemyClientRepository(
        update_session,
        cache,
    ).update_client(Client(id=903, client_name="C3b"))
    update_session.commit()
    update_session.close()

    read_session = Session(db_engine)
    updated = SqlAlchemyClientRepository(read_session, cache).get_client(903)
    assert updated is not None
    assert updated.client_name == "C3b"
    assert [item.project_title for item in updated.projects] == ["Project"]
    read_session.close()

    cleanup_session = Session(db_engine)
    cleanup_session.query(Project).filter_by(client_id=903).delete()
    cleanup_session.query(Client).filter_by(id=903).delete()
    cleanup_session.commit()
    cleanup_session.close()
