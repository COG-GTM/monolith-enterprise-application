import pytest

from snowman.domain.exception import BusinessError
from snowman.domain.model.app_info import AppInfo
from snowman.domain.service.app_info import ApplicationInfoService


class RepositoryStub:
    def __init__(self, app_info_map: dict[int, AppInfo] | None) -> None:
        self.app_info_map = app_info_map

    def initialize(self) -> None:
        pass

    def get_app_info_map(self) -> dict[int, AppInfo] | None:
        return self.app_info_map


def test_empty_map_raises_business_error() -> None:
    service = ApplicationInfoService(RepositoryStub({}))

    with pytest.raises(BusinessError, match="^AppInfo is null or empty$"):
        service.get_app_info()


def test_none_map_raises_business_error() -> None:
    service = ApplicationInfoService(RepositoryStub(None))

    with pytest.raises(BusinessError, match="^AppInfo is null or empty$"):
        service.get_app_info()


def test_multiple_entries_raise_business_error() -> None:
    service = ApplicationInfoService(
        RepositoryStub({1: AppInfo(id=1, version="1.0.0"), 2: AppInfo(id=2, version="2.0.0")})
    )

    with pytest.raises(
        BusinessError,
        match="^There are more than one entry in AppInfo$",
    ):
        service.get_app_info()


def test_single_entry_is_returned() -> None:
    expected = AppInfo(id=1, version="1.0.0")
    service = ApplicationInfoService(RepositoryStub({1: expected}))

    assert service.get_app_info() is expected
