import logging
from collections.abc import Callable

from snowman.domain.model.app_info import AppInfo

logger = logging.getLogger(__name__)


class ApplicationInfoRepositoryImpl:
    def __init__(self, load_app_infos: Callable[[], list[AppInfo]]) -> None:
        self._load_app_infos = load_app_infos
        self._app_info_map: dict[int, AppInfo] = {}
        self._initialized = False

    def initialize(self) -> None:
        if self._initialized:
            return
        logger.info("Loading AppInfo from Database")
        self._app_info_map = {
            app_info.id: app_info for app_info in self._load_app_infos()
        }
        self._initialized = True

    def get_app_info_map(self) -> dict[int, AppInfo]:
        return self._app_info_map
