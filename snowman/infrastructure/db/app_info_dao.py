import logging

from sqlalchemy import text
from sqlalchemy.exc import SQLAlchemyError
from sqlalchemy.orm import Session

from snowman.domain.model.app_info import AppInfo

logger = logging.getLogger(__name__)


def load_application_infos(session: Session) -> list[AppInfo]:
    logger.info("Loading Application Infos from the database...")
    try:
        rows = session.execute(text("SELECT * FROM app_info")).mappings().all()
        return [
            AppInfo(id=int(row["id"]), version=row["version"])
            for row in rows
        ]
    except SQLAlchemyError:
        logger.exception("Unable to load Application Infos from the database")
        return []
