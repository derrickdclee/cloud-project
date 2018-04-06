from rest_framework import permissions


class IsHost(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return obj.host == request.user


class IsSameUser(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return obj == request.user


class IsHostOrInvitee(permissions.BasePermission):
    """
    TODO: not very OO...
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        is_admin = request.user and request.user.is_staff
        return (obj.party.host == request.user) \
               or (obj.invitee == request.user)\
               or is_admin