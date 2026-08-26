"""User REST endpoints."""

from fastapi import APIRouter, Depends
from fastapi.responses import Response
from sqlalchemy.orm import Session

from snowman.db.session import get_db
from snowman.domain.exception import EntityNotFoundError
from snowman.domain.service.user import UserService
from snowman.infrastructure.db.user_dao import SqlUserDao
from snowman.infrastructure.rest.mappers.user import to_resource, to_user
from snowman.infrastructure.rest.resources.user import UserResource

router = APIRouter(prefix="/user", tags=["user"])


def _service(db: Session) -> UserService:
    return UserService(SqlUserDao(db))


@router.get("/{userId}", response_model=UserResource)
def get_user(userId: str, db: Session = Depends(get_db)) -> UserResource:
    """Return a user by its Java-compatible string id."""

    user = _service(db).find_user(userId)
    if user is None:
        raise EntityNotFoundError(f"User not found: {userId}")
    return to_resource(user)


@router.post("/create")
def create_user(resource: UserResource, db: Session = Depends(get_db)) -> Response:
    """Create a user."""

    _service(db).create_user(to_user(resource))
    return Response(status_code=200)


@router.post("/update")
def update_user(resource: UserResource, db: Session = Depends(get_db)) -> Response:
    """Update a user."""

    _service(db).update_user(to_user(resource))
    return Response(status_code=200)


@router.delete("/{userId}/delete")
def delete_user(userId: int, db: Session = Depends(get_db)) -> Response:
    """Delete a user."""

    _service(db).delete_user(userId)
    return Response(status_code=200)
