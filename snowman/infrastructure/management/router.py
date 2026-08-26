import logging
from typing import cast

from fastapi import APIRouter, Depends, Request
from sqlalchemy.orm import Session

from snowman.application.cache.client_cache_service import ClientCacheService
from snowman.application.healthcheck import HealthCheck
from snowman.db.session import get_db

router = APIRouter(tags=["management"])
logger = logging.getLogger(__name__)


@router.get("/health")
def health(session: Session = Depends(get_db)) -> dict[str, str]:
    health_check = HealthCheck(session)
    return {"status": health_check.get_health_status().value}


@router.get("/cache/{cacheName}/clear")
@router.post("/cache/{cacheName}/clear")
def clear_cache(cacheName: str, request: Request) -> dict[str, int | str]:
    cache_service = cast(ClientCacheService, request.app.state.client_cache_service)
    logger.info("About to clear %s cache", cacheName)
    cache_service.clear_cache()
    return {
        "statusCode": 200,
        "description": f"{cacheName} have been cleared",
    }
