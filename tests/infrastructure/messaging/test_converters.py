from datetime import date

from snowman.domain.model import Client, Employee, EmployeeProject, EmployeeRole, Project
from snowman.infrastructure.messaging.converter import (
    to_client_dto,
    to_employee_dto,
    to_project_dto,
)


def test_project_converter_preserves_wire_field_names() -> None:
    project = Project(
        id=4,
        project_title="Migration",
        date_started=date(2024, 2, 3),
        date_ended=None,
        client_id=12,
    )

    dto = to_project_dto(project)

    assert dto.model_dump() == {
        "projectId": 4,
        "projectTitle": "Migration",
        "dateStarted": date(2024, 2, 3),
        "dateEnded": None,
    }


def test_employee_converter_walks_employee_projects() -> None:
    project = Project(
        id=4,
        project_title="Migration",
        date_started=None,
        date_ended=None,
        client_id=12,
    )
    employee = Employee(
        id=9,
        firstname="Grace",
        surname="Hopper",
        role=EmployeeRole(id=1, role="Architect"),
        projects=[EmployeeProject(project=project)],
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
    employee = Employee(
        id=9,
        firstname="Grace",
        surname="Hopper",
        role=None,
        projects=[],
    )

    assert to_employee_dto(employee).role is None


def test_client_converter_maps_projects() -> None:
    project = Project(
        id=4,
        project_title="Migration",
        date_started=None,
        date_ended=None,
        client_id=12,
    )
    client = Client(id=12, client_name="Acme", projects=[project])

    dto = to_client_dto(client)

    assert dto.clientId == 12
    assert dto.clientName == "Acme"
    assert [item.projectId for item in dto.projectDTOS] == [4]
