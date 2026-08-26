"""Domain-facing messaging port protocols.

DTO implementations are owned by WS6. The foundation ports intentionally accept
opaque objects so they do not import infrastructure DTO modules.
"""

from typing import Protocol


class PayrollSystemPort(Protocol):
    """Port for sending employee information to payroll."""

    def send_employee_info(self, dto: object) -> None:
        """Send an employee DTO."""


class InvoiceSystemPort(Protocol):
    """Port for sending client information to invoicing."""

    def send_project_info(self, dto: object) -> None:
        """Send a client DTO."""


class NotificationPort(Protocol):
    """Port for broadcasting updates."""

    def broadcast_updates(self, payload: object) -> None:
        """Broadcast a payload."""
