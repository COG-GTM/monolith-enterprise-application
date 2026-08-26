class BusinessError(Exception):
    """Base class for business-rule failures."""


class SnowmanError(BusinessError):
    """A Snowman application error."""


class EntityNotFoundError(BusinessError):
    """An expected entity could not be found."""
