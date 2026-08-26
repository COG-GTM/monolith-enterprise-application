"""Domain exception hierarchy."""


class BusinessError(Exception):
    """Base class for business-level failures."""


class SnowmanError(BusinessError):
    """General Snowman business failure."""


class EntityNotFoundError(BusinessError):
    """Raised when a requested entity does not exist."""
