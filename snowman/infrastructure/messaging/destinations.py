from snowman.infrastructure.messaging.broker import Destination

INVOICE_SYSTEM_QUEUE = Destination("invoice-system-queue", "queue")
PAYROLL_SYSTEM_QUEUE = Destination("payroll-system-queue", "queue")
NOTIFICATION_TOPIC = Destination("notification-topic", "topic")
