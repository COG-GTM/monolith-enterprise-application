import logging

from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

logger = logging.getLogger(__name__)


def db_status(session: Session) -> bool:
    try:
        return session.execute(text("SELECT min(1) FROM app_info")).first() is not None
    except SQLAlchemyError:
        logger.exception("Unable to check database health")
        return False
