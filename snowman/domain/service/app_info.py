from snowman.domain.exception import BusinessError
from snowman.domain.model.app_info import AppInfo
from snowman.domain.repository.app_info import ApplicationInfoRepository


class ApplicationInfoService:
    def __init__(self, repository: ApplicationInfoRepository) -> None:
        self._repository = repository

    def get_app_info(self) -> AppInfo:
        app_info_map = self._repository.get_app_info_map()
        if not app_info_map:
            raise BusinessError("AppInfo is null or empty")
        if len(app_info_map) != 1:
            raise BusinessError("There are more than one entry in AppInfo")
        return next(iter(app_info_map.values()))
