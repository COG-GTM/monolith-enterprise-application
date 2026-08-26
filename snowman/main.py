"""FastAPI application factory."""

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from snowman.application.cache.client_cache_service import (
    ClientCachePort,
    ClientCacheService,
)
from snowman.config import get_settings
from snowman.db.session import SessionLocal
from snowman.domain.exception import BusinessError, EntityNotFoundError, SnowmanError
from snowman.domain.model.app_info import AppInfo
from snowman.domain.repository.impl.app_info import ApplicationInfoRepositoryImpl
from snowman.domain.service.app_info import ApplicationInfoService
from snowman.infrastructure.db.app_info_dao import load_application_infos
from snowman.infrastructure.management.router import router as management_router
from snowman.infrastructure.rest.routers.app_info import router as app_info_router


class _NoOpClientCachePort:
    """Default wiring until WS7 provides the concrete cache adapter."""

    def refresh_cache(self) -> None:
        pass


def _load_app_infos() -> list[AppInfo]:
    session = SessionLocal()
    try:
        return load_application_infos(session)
    finally:
        session.close()


async def _business_error_handler(_: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(status_code=500, content={"detail": str(exc)})


async def _not_found_error_handler(_: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(status_code=404, content={"detail": str(exc)})


@asynccontextmanager
async def _lifespan(application: FastAPI) -> AsyncIterator[None]:
    """Manage optional infrastructure services.

    WS7 owns the scheduler implementation. Until it is available, the enabled branch
    intentionally remains a no-op while retaining the application lifecycle seam.
    """

    if get_settings().scheduler_enabled:
        # WS7 will start the scheduler here.
        pass
    application.state.application_info_repository.initialize()
    yield
    if get_settings().scheduler_enabled:
        # WS7 will stop the scheduler here.
        pass


def create_app() -> FastAPI:
    """Build and configure the Snowman FastAPI application."""

    repository = ApplicationInfoRepositoryImpl(_load_app_infos)
    application = FastAPI(title="Snowman", version="1.0.0", lifespan=_lifespan)
    application.state.application_info_repository = repository
    application.state.app_info_service = ApplicationInfoService(repository)
    cache_port: ClientCachePort = _NoOpClientCachePort()
    application.state.client_cache_service = ClientCacheService(cache_port)
    application.add_exception_handler(SnowmanError, _business_error_handler)
    application.add_exception_handler(BusinessError, _business_error_handler)
    application.add_exception_handler(EntityNotFoundError, _not_found_error_handler)

    # --- routers ---
    # WS1–WS5 append exactly one app.include_router(...) line each here.
    application.include_router(app_info_router)
    application.include_router(management_router)

    return application


app = create_app()


def main() -> None:
    """Run the application with uvicorn."""

    uvicorn.run("snowman.main:app", host="0.0.0.0", port=get_settings().port)
