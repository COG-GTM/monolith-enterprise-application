from typing import Protocol

from snowman.infrastructure.messaging.dto import ClientDTO, EmployeeDTO


class PayrollSystemPort(Protocol):
    def send_employee_info(self, dto: EmployeeDTO) -> None:
        ...


class InvoiceSystemPort(Protocol):
    def send_project_info(self, dto: ClientDTO) -> None:
        ...


class NotificationPort(Protocol):
    def broadcast_updates(self, payload: object) -> None:
        ...
