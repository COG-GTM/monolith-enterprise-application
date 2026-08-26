"""Explicit client cache abstraction.

The Java cache used LFU eviction and could overflow entries to ``localTempSwap``.
This port intentionally accepts the simplifications of heap-only storage and
cachetools' default eviction policy.
"""

from __future__ import annotations

import time
from collections.abc import Callable
from threading import RLock
from typing import Protocol

from cachetools import TTLCache

from snowman.config import Settings, get_settings
from snowman.domain.model import Client


class ClientCache(Protocol):
    """Port for caching clients by identifier."""

    def get(self, client_id: int) -> Client | None:
        """Return a cached client, if it has not expired."""

    def put(self, client_id: int, client: Client) -> None:
        """Store a client in the cache."""

    def evict(self, client_id: int) -> None:
        """Remove one client from the cache."""

    def clear(self) -> None:
        """Remove all clients from the cache."""


class TTLClientCache:
    """In-memory client cache with absolute TTL and per-key idle expiry.

    Access is guarded by one reentrant lock for thread safety.
    """

    def __init__(
        self,
        settings: Settings | None = None,
        clock: Callable[[], float] = time.monotonic,
    ) -> None:
        configured = settings or get_settings()
        self._clock = clock
        self._cache: TTLCache[int, Client] = TTLCache(
            maxsize=configured.cache_max_entries,
            ttl=configured.cache_ttl_seconds,
            timer=clock,
        )
        self._tti_seconds = configured.cache_tti_seconds
        self._last_access: dict[int, float] = {}
        self._lock = RLock()

    def _prune_last_access(self) -> None:
        cache_keys = set(self._cache)
        self._last_access = {
            client_id: last_access
            for client_id, last_access in self._last_access.items()
            if client_id in cache_keys
        }

    def get(self, client_id: int) -> Client | None:
        with self._lock:
            self._cache.expire()
            last_access = self._last_access.get(client_id)
            if last_access is None:
                return None
            now = self._clock()
            if now - last_access >= self._tti_seconds:
                self._cache.pop(client_id, None)
                self._last_access.pop(client_id, None)
                return None
            client = self._cache.get(client_id)
            if client is None:
                self._last_access.pop(client_id, None)
                return None
            self._last_access[client_id] = now
            return client

    def put(self, client_id: int, client: Client) -> None:
        with self._lock:
            self._cache[client_id] = client
            self._last_access[client_id] = self._clock()
            self._cache.expire()
            self._prune_last_access()

    def evict(self, client_id: int) -> None:
        with self._lock:
            self._cache.pop(client_id, None)
            self._last_access.pop(client_id, None)

    def clear(self) -> None:
        with self._lock:
            self._cache.clear()
            self._last_access.clear()
