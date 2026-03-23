"""FastAPI application entry-point for the Python User-Service.

This service replaces the Spring Security / Keycloak adapter that previously
guarded the Java ``UserRestEndpoint``.  It validates Keycloak-issued JWTs,
enforces role-based access via FastAPI dependencies, and proxies authorised
requests to the upstream Snowman monolith.
"""

from __future__ import annotations

import logging

from fastapi import FastAPI

from app.routers.users import router as users_router

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Snowman User-Service (Python/FastAPI)",
    description=(
        "JWT-secured User-Service backed by Keycloak. "
        "Replaces the Spring Security Keycloak adapter with "
        "python-keycloak and FastAPI dependency-based RBAC."
    ),
    version="1.0.0",
)

app.include_router(users_router)


@app.get("/health", tags=["management"])
async def health() -> dict[str, str]:
    """Simple liveness probe."""
    return {"status": "UP"}
