from typing import Protocol

from snowman.domain.model.app_info import AppInfo


class ApplicationInfoRepository(Protocol):
    def initialize(self) -> None:
        ...

    def get_app_info_map(self) -> dict[int, AppInfo]:
        ...
