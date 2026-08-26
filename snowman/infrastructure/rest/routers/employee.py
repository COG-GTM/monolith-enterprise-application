"""Employee REST endpoints."""

from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import Response
from sqlalchemy.orm import Session

from snowman.db.session import get_db
from snowman.domain.repository.impl.employee import SqlAlchemyEmployeeRepository
from snowman.domain.service.employee import EmployeeService
from snowman.infrastructure.rest.mappers.employee import to_employee, to_resource
from snowman.infrastructure.rest.resources.employee import EmployeeResource

router = APIRouter(prefix="/employee", tags=["employee"])


def _employee_service(session: Session = Depends(get_db)) -> EmployeeService:
    return EmployeeService(SqlAlchemyEmployeeRepository(session))


@router.get("/{employeeId}", response_model=EmployeeResource)
def get_employee(
    employeeId: int,
    service: EmployeeService = Depends(_employee_service),
) -> EmployeeResource:
    employee = service.get_employee(employeeId)
    if employee is None:
        raise HTTPException(
            status_code=404,
            detail=f"There is no existing employee with id: {employeeId}",
        )
    return to_resource(employee)


@router.post("/create", response_class=Response, status_code=200)
def create_employee(
    resource: EmployeeResource,
    service: EmployeeService = Depends(_employee_service),
) -> Response:
    service.create_employee(to_employee(resource))
    return Response(status_code=200)


@router.post("/update", response_class=Response, status_code=200)
def update_employee(
    resource: EmployeeResource,
    service: EmployeeService = Depends(_employee_service),
) -> Response:
    service.update_employee(to_employee(resource))
    return Response(status_code=200)


@router.delete("/{employeeId}/delete", response_class=Response, status_code=200)
def delete_employee(
    employeeId: int,
    service: EmployeeService = Depends(_employee_service),
) -> Response:
    service.delete_employee(employeeId)
    return Response(status_code=200)
