"""Cache refresh port and its in-process adapter."""

from __future__ import annotations

import logging
from typing import Protocol

from snowman.infrastructure.cache.client_cache import ClientCache
from snowman.infrastructure.cache.deps import get_client_cache

logger = logging.getLogger(__name__)


class ClientCachePort(Protocol):
    """Port used by cache-management application services."""

    def refresh_cache(self) -> None:
        """Clear all cached clients."""


class ClientCacheAdapter:
    """Adapter exposing refresh semantics for the shared client cache."""

    def __init__(self, cache: ClientCache | None = None) -> None:
        self._cache = cache if cache is not None else get_client_cache()

    def refresh_cache(self) -> None:
        logger.info("Executing clearing Client Cache operation")
        self._cache.clear()
