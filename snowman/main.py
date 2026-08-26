import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from snowman.config import get_settings
from snowman.domain.exception import BusinessError, EntityNotFoundError, SnowmanError


def create_app() -> FastAPI:
    app = FastAPI(title="Snowman", version="1.0.0")

    @app.exception_handler(EntityNotFoundError)
    async def entity_not_found_handler(
        request: Request, exc: EntityNotFoundError
    ) -> JSONResponse:
        return JSONResponse(status_code=404, content={"detail": str(exc)})

    @app.exception_handler(SnowmanError)
    @app.exception_handler(BusinessError)
    async def business_error_handler(request: Request, exc: BusinessError) -> JSONResponse:
        return JSONResponse(status_code=500, content={"detail": str(exc)})

    # --- routers ---
    return app


app = create_app()


def main() -> None:
    uvicorn.run("snowman.main:app", host="0.0.0.0", port=get_settings().port)
