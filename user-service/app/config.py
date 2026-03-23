"""Application configuration loaded from environment variables."""

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Keycloak and application settings.

    All values are read from environment variables (or a .env file).
    """

    # Keycloak connection
    keycloak_server_url: str = "http://localhost:8080"
    keycloak_realm: str = "snowman"
    keycloak_client_id: str = "user-service"
    keycloak_client_secret: str = ""

    # JWT validation
    jwt_algorithm: str = "RS256"

    # Upstream Java service (Snowman monolith)
    upstream_base_url: str = "http://localhost:8090"

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


settings = Settings()
