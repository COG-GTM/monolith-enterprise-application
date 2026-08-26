"""Employee router integration tests."""

from sqlalchemy.orm import Session

from snowman.domain.model.employee import EmployeeRole


def _seed_role(db_session: Session) -> None:
    db_session.add(EmployeeRole(id=1, role="Engineer"))
    db_session.flush()


def test_employee_endpoints_and_payload_contract(client, db_session: Session) -> None:
    _seed_role(db_session)
    payload = {
        "employeeId": 0,
        "firstName": "First",
        "secondName": "Surname",
        "role": "Engineer",
    }

    create_response = client.post("/employee/create", json=payload)
    assert create_response.status_code == 200
    assert create_response.content == b""

    employee_response = client.get("/employee/1")
    assert employee_response.status_code == 200
    assert employee_response.json() == {
        "employeeId": 1,
        "firstName": "First",
        "secondName": "Surname",
        "role": "Engineer",
    }

    update_response = client.post(
        "/employee/update",
        json={**payload, "employeeId": 1, "firstName": "Updated"},
    )
    assert update_response.status_code == 200
    assert update_response.content == b""
    assert client.get("/employee/1").json()["firstName"] == "Updated"

    delete_response = client.delete("/employee/1/delete")
    assert delete_response.status_code == 200
    assert delete_response.content == b""
    assert client.get("/employee/1").status_code == 404


def test_employee_unknown_endpoints_return_404(client) -> None:
    assert client.get("/employee/999").status_code == 404
    assert client.post(
        "/employee/update",
        json={
            "employeeId": 999,
            "firstName": "First",
            "secondName": "Surname",
            "role": None,
        },
    ).status_code == 404
    assert client.delete("/employee/999/delete").status_code == 404


def test_employee_openapi_lists_four_paths(client) -> None:
    paths = client.get("/openapi.json").json()["paths"]

    assert {path for path in paths if path.startswith("/employee/")} == {
        "/employee/{employeeId}",
        "/employee/create",
        "/employee/update",
        "/employee/{employeeId}/delete",
    }
