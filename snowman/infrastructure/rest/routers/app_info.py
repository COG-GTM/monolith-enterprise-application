from typing import cast

from fastapi import APIRouter, Request

from snowman.domain.service.app_info import ApplicationInfoService
from snowman.infrastructure.rest.mappers.app_info import map_app_info_to_resource
from snowman.infrastructure.rest.resources.app_info import AppInfoResource

router = APIRouter(tags=["app"])


@router.get("/app/info", response_model=AppInfoResource)
def get_app_info(request: Request) -> AppInfoResource:
    service = cast(ApplicationInfoService, request.app.state.app_info_service)
    return map_app_info_to_resource(service.get_app_info())
