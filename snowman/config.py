from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix="SNOWMAN_",
        env_file=".env",
        extra="ignore",
    )

    port: int = 8090
    database_url: str = "sqlite:///./snowman.db"
    echo_sql: bool = True
    broker_url: str = "memory://"
    scheduler_enabled: bool = True
    client_system_url: str = "http://localhost:8080/client-system/client/{clientId}/projects"
    cache_max_entries: int = 10000
    cache_ttl_seconds: int = 600
    cache_tti_seconds: int = 300


@lru_cache
def get_settings() -> Settings:
    return Settings()
