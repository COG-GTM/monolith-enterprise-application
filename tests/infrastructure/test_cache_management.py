from snowman.application.cache.client_cache_service import ClientCacheService


class FakeClientCachePort:
    def __init__(self) -> None:
        self.refresh_calls = 0

    def refresh_cache(self) -> None:
        self.refresh_calls += 1


def test_cache_management_endpoint_returns_exact_body(client) -> None:
    fake_port = FakeClientCachePort()
    client.app.state.client_cache_service = ClientCacheService(fake_port)

    response = client.get("/cache/clientFindCache/clear")

    assert response.status_code == 200
    assert response.json() == {
        "statusCode": 200,
        "description": "clientFindCache have been cleared",
    }
    assert fake_port.refresh_calls == 1
