# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from rest_framework import generics, permissions
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework.reverse import reverse
from rest_condition import And, Or, Not

from project.api.permissions import IsGetRequest, IsHostOfParty, IsSameUser, \
    IsHostOrInvitee, IsSameUserWithParam, IsHostOfInvitation
from project.api.models import Party, Invitation
from django.contrib.auth.models import User
from project.api.serializers import PartySerializer, UserSerializer, InvitationSerializer


@api_view(['GET'])
def api_root(request, format=None):
    return Response({
        'users': reverse('user-list', request=request, format=format),
        'parties': reverse('party-list', request=request, format=format)
    })


class PartyList(generics.ListCreateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser,
                             And(permissions.IsAuthenticated, Not(IsGetRequest))),)

    def perform_create(self, serializer):
        serializer.save(host=self.request.user)


class PartyDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser, IsHostOfParty), )


class UserList(generics.ListAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (permissions.IsAdminUser,)


class UserDetail(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (Or(permissions.IsAdminUser, IsSameUser),)


class InvitationList(generics.ListCreateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(permissions.IsAdminUser,
                             And(IsHostOfInvitation, Not(IsGetRequest))),)

    # this is a hook, called when creating an instance
    # need to override this method as we are writing to ReadOnlyField
    def perform_create(self, serializer):
        invitee = User.objects.get(pk=self.request.data['invitee_id'])
        party = Party.objects.get(pk=self.request.data['party_id'])
        serializer.save(invitee=invitee, party=party)


class InvitationDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(permissions.IsAdminUser, IsHostOrInvitee),)


class HostedPartyList(generics.ListAPIView):
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser, IsSameUserWithParam),)

    def get_queryset(self):
        host_id = self.kwargs['user_id']
        return Party.objects.filter(host=host_id)


class InvitedPartyList(generics.ListAPIView):
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser, IsSameUserWithParam),)

    def get_queryset(self):
        invitee_id = self.kwargs['user_id']
        """
        note this weird '__in' syntax for filtering on ManyToManyField
        """
        return Party.objects.filter(invitees__in=invitee_id)


class InvitationToPartyList(generics.ListAPIView):
    serializer_class = InvitationSerializer
    permission_classes = (permissions.IsAuthenticated,)
    #TODO: improve this permission

    def get_queryset(self):
        party_id = self.kwargs['party_id']
        return Invitation.objects.filter(party=party_id)

