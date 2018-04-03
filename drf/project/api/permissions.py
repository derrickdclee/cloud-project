from rest_framework import permissions


class IsSameUser(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        # permissions are only allowed to the owner of the snippet.
        return obj == request.user