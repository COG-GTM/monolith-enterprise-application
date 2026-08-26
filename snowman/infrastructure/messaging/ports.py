"""Domain-facing messaging port protocols.

DTO implementations are owned by WS6. The foundation ports intentionally accept
opaque objects so they do not import infrastructure DTO modules.
"""

from typing import Any, Protocol


class PayrollSystemPort(Protocol):
    """Port for sending employee information to payroll."""

    # WS6 supplies EmployeeDTO instances.
    def send_employee_info(self, dto: Any) -> None:
        """Send an employee DTO."""


class InvoiceSystemPort(Protocol):
    """Port for sending client information to invoicing."""

    # WS6 supplies ClientDTO instances.
    def send_project_info(self, dto: Any) -> None:
        """Send a client DTO."""


class NotificationPort(Protocol):
    """Port for broadcasting updates."""

    # WS6 may supply any payload.
    def broadcast_updates(self, payload: Any) -> None:
        """Broadcast a payload."""
