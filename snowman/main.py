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
from snowman.infrastructure.cache.client_cache_port import ClientCacheAdapter
from snowman.infrastructure.db.app_info_dao import load_application_infos
from snowman.infrastructure.management.router import router as management_router
from snowman.infrastructure.rest.routers.app_info import router as app_info_router
from snowman.infrastructure.rest.routers.employee import router as employee_router
from snowman.infrastructure.rest.routers import client as client_router
from snowman.infrastructure.rest.routers.project import router as project_router
from snowman.infrastructure.rest.routers.user import router as user_router
from snowman.infrastructure.scheduling.scheduler import shutdown_scheduler, start_scheduler


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
    if get_settings().scheduler_enabled:
        start_scheduler(application)
    application.state.application_info_repository.initialize()
    yield
    if get_settings().scheduler_enabled:
        shutdown_scheduler()


def create_app() -> FastAPI:
    """Build and configure the Snowman FastAPI application."""

    repository = ApplicationInfoRepositoryImpl(_load_app_infos)
    application = FastAPI(title="Snowman", version="1.0.0", lifespan=_lifespan)
    application.state.application_info_repository = repository
    application.state.app_info_service = ApplicationInfoService(repository)
    cache_port: ClientCachePort = ClientCacheAdapter()
    application.state.client_cache_service = ClientCacheService(cache_port)
    application.add_exception_handler(SnowmanError, _business_error_handler)
    application.add_exception_handler(BusinessError, _business_error_handler)
    application.add_exception_handler(EntityNotFoundError, _not_found_error_handler)

    # --- routers ---
    # WS1–WS5 append exactly one app.include_router(...) line each here.
    application.include_router(employee_router)
    application.include_router(client_router.router)
    application.include_router(project_router)
    application.include_router(user_router)
    application.include_router(app_info_router)
    application.include_router(management_router)

    return application


app = create_app()


def main() -> None:
    """Run the application with uvicorn."""

    uvicorn.run("snowman.main:app", host="0.0.0.0", port=get_settings().port)
