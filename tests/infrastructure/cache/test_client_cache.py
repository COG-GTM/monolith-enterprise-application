"""Tests for the client cache abstraction."""

from snowman.config import Settings
from snowman.domain.model import Client
from snowman.infrastructure.cache.client_cache import TTLClientCache
from snowman.infrastructure.cache.client_cache_port import ClientCacheAdapter
from snowman.infrastructure.cache.deps import get_client_cache


def test_put_get_evict_and_clear() -> None:
    cache = TTLClientCache(
        Settings(cache_max_entries=1, cache_ttl_seconds=600, cache_tti_seconds=300)
    )
    client = Client(id=1, client_name="one")
    second = Client(id=2, client_name="two")

    cache.put(1, client)
    assert cache.get(1) is client
    cache.put(2, second)
    assert cache.get(1) is None
    assert cache.get(2) is second
    cache.evict(2)
    assert cache.get(2) is None
    cache.put(1, client)
    cache.clear()
    assert cache.get(1) is None


def test_ttl_expiry_with_injected_clock() -> None:
    current = [100.0]
    settings = Settings(cache_max_entries=10, cache_ttl_seconds=10, cache_tti_seconds=100)
    cache = TTLClientCache(settings, clock=lambda: current[0])
    cache.put(1, Client(id=1, client_name="one"))

    current[0] = 109.9
    assert cache.get(1) is not None
    current[0] = 110.0
    assert cache.get(1) is None


def test_idle_expiry_with_injected_clock() -> None:
    current = [100.0]
    settings = Settings(cache_max_entries=10, cache_ttl_seconds=100, cache_tti_seconds=10)
    cache = TTLClientCache(settings, clock=lambda: current[0])
    client = Client(id=1, client_name="one")
    cache.put(1, client)

    current[0] = 109.0
    assert cache.get(1) is client
    current[0] = 118.9
    assert cache.get(1) is client
    current[0] = 129.0
    assert cache.get(1) is None


def test_get_client_cache_is_process_wide_singleton() -> None:
    get_client_cache.cache_clear()
    first = get_client_cache()
    second = get_client_cache()
    assert first is second
    get_client_cache.cache_clear()


def test_cache_adapter_refreshes_shared_cache() -> None:
    get_client_cache.cache_clear()
    cache = get_client_cache()
    cache.put(1, Client(id=1, client_name="one"))

    ClientCacheAdapter().refresh_cache()

    assert cache.get(1) is None
    get_client_cache.cache_clear()
