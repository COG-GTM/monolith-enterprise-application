from datetime import date
from types import SimpleNamespace

from snowman.infrastructure.messaging.converter import (
    to_client_dto,
    to_employee_dto,
    to_project_dto,
)


def test_project_converter_preserves_wire_field_names() -> None:
    project = SimpleNamespace(
        id=4,
        project_title="Migration",
        date_started=date(2024, 2, 3),
        date_ended=None,
    )

    dto = to_project_dto(project)

    assert dto.model_dump() == {
        "projectId": 4,
        "projectTitle": "Migration",
        "dateStarted": date(2024, 2, 3),
        "dateEnded": None,
    }


def test_employee_converter_walks_employee_projects() -> None:
    project = SimpleNamespace(
        id=4,
        project_title="Migration",
        date_started=None,
        date_ended=None,
    )
    employee = SimpleNamespace(
        id=9,
        firstname="Grace",
        surname="Hopper",
        role=SimpleNamespace(role="Architect"),
        projects=[SimpleNamespace(project=project)],
    )

    dto = to_employee_dto(employee)

    assert dto.model_dump() == {
        "id": 9,
        "firstName": "Grace",
        "surname": "Hopper",
        "role": "Architect",
        "projectDTOList": [
            {
                "projectId": 4,
                "projectTitle": "Migration",
                "dateStarted": None,
                "dateEnded": None,
            }
        ],
    }


def test_employee_converter_emits_none_for_missing_role() -> None:
    employee = SimpleNamespace(
        id=9,
        firstname="Grace",
        surname="Hopper",
        role=None,
        projects=[],
    )

    assert to_employee_dto(employee).role is None


def test_client_converter_maps_projects() -> None:
    project = SimpleNamespace(
        id=4,
        project_title="Migration",
        date_started=None,
        date_ended=None,
    )
    client = SimpleNamespace(id=12, client_name="Acme", projects=[project])

    dto = to_client_dto(client)

    assert dto.clientId == 12
    assert dto.clientName == "Acme"
    assert [item.projectId for item in dto.projectDTOS] == [4]
