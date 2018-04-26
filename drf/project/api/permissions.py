from rest_framework import permissions
from django.contrib.auth.models import User
from project.api.models import Party


class IsAdmin(permissions.IsAdminUser):
    """
    Need to override the permissions.IsAdminUser because of a bug in rest_conditions library
    whereby mixing permissions that implements has_object_permission with those that don't lead to
    the whole condition being short-circuited
    """
    def has_permission(self, request, view):
        return True

    def has_object_permission(self, request, view, obj):
        return super(IsAdmin, self).has_permission(request, view)


class IsAuth(permissions.IsAuthenticated):
    def has_permission(self, request, view):
        return True

    def has_object_permission(self, request, view, obj):
        return super(IsAuth, self).has_permission(request, view)


class IsGetRequest(permissions.BasePermission):
    def has_permission(self, request, view):
        return request.method == 'GET'


class IsSameUserObject(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return obj == request.user


class IsSameUserWithURLParam(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_permission(self, request, view):
        try:
            user = User.objects.get(pk=view.kwargs['user_id'])
        except User.DoesNotExist:
            return False
        return user == request.user


class IsHostOfPartyObject(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_object_permission(self, request, view, obj):
        return obj.host == request.user


class IsHostOfPartyObjectOrReadOnly(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        if request.method == 'GET':
            return True
        else:
            test = IsHostOfPartyObject()
            return test.has_object_permission(request, view, obj)


class IsHostOfPartyWithURLParam(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_permission(self, request, view):
        try:
            party = Party.objects.get(pk=view.kwargs['pk'])
        except Party.DoesNotExist:
            return False
        return party.host == request.user


class IsHostOfPartyWithParam(permissions.BasePermission):
    """
    Custom permission to only allow owners of an object to edit it.
    """

    def has_permission(self, request, view):
        party_id = request.data.get('party_id')
        if party_id is None:
            return False
        try:
            party = Party.objects.get(pk=party_id)
        except Party.DoesNotExist:
            return False
        return party.host == request.user


class IsHostOfInvitationObject(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        return obj.party.host == request.user


class IsBouncerOfInvitationObject(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        return request.user in obj.party.bouncers.all()


class IsInviteeOfInvitationObject(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        return obj.invitee == request.user


class IsHostOrBouncer(permissions.BasePermission):
    def has_permission(self, request, view):
        try:
            party = Party.objects.get(pk=view.kwargs['party_id'])
        except Party.DoesNotExist:
            return False
        # note that we need .all() on ManyToManyFields
        return party.host == request.user or request.user in party.bouncers.all()
