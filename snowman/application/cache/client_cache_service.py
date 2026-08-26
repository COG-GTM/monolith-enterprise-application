import logging
from typing import Protocol

logger = logging.getLogger(__name__)


class ClientCachePort(Protocol):
    """Mirrors the ClientCachePort Protocol declared by WS7."""

    def refresh_cache(self) -> None:
        ...


class ClientCacheService:
    def __init__(self, cache_port: ClientCachePort) -> None:
        self._cache_port = cache_port

    def clear_cache(self) -> None:
        logger.info("Clearing Client Cache")
        self._cache_port.refresh_cache()
        logger.info("Cleared Client Cache")
