"""Dependency providers for cache infrastructure."""

from functools import lru_cache

from snowman.infrastructure.cache.client_cache import ClientCache, TTLClientCache


@lru_cache(maxsize=1)
def get_client_cache() -> ClientCache:
    """Return the process-wide client cache singleton."""

    return TTLClientCache()
