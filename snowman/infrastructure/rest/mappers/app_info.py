from snowman.domain.model.app_info import AppInfo
from snowman.infrastructure.rest.resources.app_info import AppInfoResource


def map_app_info_to_resource(app_info: AppInfo) -> AppInfoResource:
    return AppInfoResource(id=app_info.id, version=app_info.version)
