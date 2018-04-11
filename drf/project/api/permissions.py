from rest_framework import permissions
from django.contrib.auth.models import User
from project.api.models import Party


class IsAdmin(permissions.IsAdminUser):
    def has_permission(self, request, view):
        return True

    def has_object_permission(self, request, view, obj):
        return super(IsAdmin, self).has_permission(request, view)


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
        return User.objects.get(pk=view.kwargs['user_id']) == request.user


class IsHostOfInvitation(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_permission(self, request, view):
        party = Party.objects.get(pk=request.data['party_id'])
        return party.host == request.user


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
