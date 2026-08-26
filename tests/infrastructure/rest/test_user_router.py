"""User router tests."""

from fastapi.testclient import TestClient

USER = {
    "userId": 1,
    "username": "alice",
    "password": "secret",
    "email": "alice@example.com",
    "firstName": "Alice",
    "secondName": "Example",
}


def test_user_routes_statuses_and_resource_keys(client: TestClient) -> None:
    assert client.post("/user/create", json=USER).status_code == 200

    response = client.get("/user/1")
    assert response.status_code == 200
    assert set(response.json()) == {
        "userId",
        "username",
        "password",
        "email",
        "firstName",
        "secondName",
    }

    updated = {**USER, "username": "updated"}
    assert client.post("/user/update", json=updated).status_code == 200
    assert client.get("/user/1").json()["username"] == "updated"
    assert client.delete("/user/1/delete").status_code == 200
    assert client.get("/user/1").status_code == 404


def test_user_route_invalid_id_returns_500(client: TestClient) -> None:
    response = client.get("/user/abc")

    assert response.status_code == 500
    assert response.json() == {"detail": "Invalid user id: abc"}
