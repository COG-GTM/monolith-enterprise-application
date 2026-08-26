"""Messaging dependencies.

Nothing in the Java code calls these ports either: the Java adapters are wired but unused.
Therefore no router or service calls them.
"""

from functools import lru_cache

from snowman.config import get_settings
from snowman.infrastructure.messaging.adapters import (
    InvoiceSystemAdapter,
    NotificationAdapter,
    PayrollSystemAdapter,
)
from snowman.infrastructure.messaging.broker import MessageBroker
from snowman.infrastructure.messaging.factory import build_broker
from snowman.infrastructure.messaging.ports import (
    InvoiceSystemPort,
    NotificationPort,
    PayrollSystemPort,
)


@lru_cache
def _get_broker() -> MessageBroker:
    return build_broker(get_settings())


def get_payroll_port() -> PayrollSystemPort:
    return PayrollSystemAdapter(_get_broker())


def get_invoice_port() -> InvoiceSystemPort:
    return InvoiceSystemAdapter(_get_broker())


def get_notification_port() -> NotificationPort:
    return NotificationAdapter(_get_broker())
