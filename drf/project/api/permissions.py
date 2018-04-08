from rest_framework import permissions


class IsGetRequest(permissions.BasePermission):
    def has_permission(self, request, view):
        return request.method == 'GET'


class IsSameUser(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return obj == request.user


class IsSameUserWithParam(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_permission(self, request, view):
        # very hacky solution..
        return int(view.kwargs['user_id']) == request.user.pk


class IsHostOfParty(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return obj.host == request.user


class IsHostOrInvitee(permissions.BasePermission):
    """
    TODO: not very OO...
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return (obj.party.host == request.user) or (obj.invitee == request.user)