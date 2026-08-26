"""FastAPI application factory."""

from collections.abc import AsyncIterator
from contextlib import asynccontextmanager

import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from snowman.config import get_settings
from snowman.domain.exception import BusinessError, EntityNotFoundError, SnowmanError
from snowman.infrastructure.scheduling.scheduler import shutdown_scheduler, start_scheduler


async def _business_error_handler(_: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(status_code=500, content={"detail": str(exc)})


async def _not_found_error_handler(_: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(status_code=404, content={"detail": str(exc)})


@asynccontextmanager
async def _lifespan(_: FastAPI) -> AsyncIterator[None]:
    """Manage optional infrastructure services.

    WS7 owns the scheduler implementation. Until it is available, the enabled branch
    intentionally remains a no-op while retaining the application lifecycle seam.
    """

    if get_settings().scheduler_enabled:
        start_scheduler(_)
    yield
    if get_settings().scheduler_enabled:
        shutdown_scheduler(_)


def create_app() -> FastAPI:
    """Build and configure the Snowman FastAPI application."""

    application = FastAPI(title="Snowman", version="1.0.0", lifespan=_lifespan)
    application.add_exception_handler(SnowmanError, _business_error_handler)
    application.add_exception_handler(BusinessError, _business_error_handler)
    application.add_exception_handler(EntityNotFoundError, _not_found_error_handler)

    # --- routers ---
    # WS1–WS5 append exactly one app.include_router(...) line each here.

    return application


app = create_app()


def main() -> None:
    """Run the application with uvicorn."""

    uvicorn.run("snowman.main:app", host="0.0.0.0", port=get_settings().port)
