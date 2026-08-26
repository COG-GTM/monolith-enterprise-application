"""User REST/domain mappings."""

from snowman.domain.model.user import User
from snowman.infrastructure.rest.resources.user import UserResource


def to_resource(user: User) -> UserResource:
    """Map a domain user to its REST representation."""

    return UserResource(
        userId=user.user_id,
        username=user.username,
        password=user.password,
        email=user.email,
        firstName=user.firstname,
        secondName=user.lastname,
    )


def to_user(resource: UserResource) -> User:
    """Map a REST resource to a domain user."""

    return User(
        user_id=resource.userId,
        username=resource.username,
        password=resource.password,
        email=resource.email,
        firstname=resource.firstName,
        lastname=resource.secondName,
    )
