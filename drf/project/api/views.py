# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework.reverse import reverse

from project.api.models import Party, Invitation
from django.contrib.auth.models import User
from project.api.serializers import PartySerializer, \
    UserSerializer, InvitationSerializer
from project.api.permissions import IsHostOrInvitee

@api_view(['GET'])
def api_root(request, format=None):
    return Response({
        'users': reverse('user-list', request=request, format=format),
        'parties': reverse('party-list', request=request, format=format)
    })


class PartyList(generics.ListCreateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    permission_classes = (permissions.IsAdminUser,)

    def perform_create(self, serializer):
        serializer.save(host=self.request.user)


class PartyDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    # TODO: Permission class


class UserList(generics.ListAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (permissions.IsAdminUser,)


class UserDetail(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    # TODO: Permission class


class InvitationList(generics.ListCreateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (permissions.IsAdminUser,)

    # this is a hook, called when creating an instance
    # need to override this method as we are writing to ReadOnlyField
    def perform_create(self, serializer):
        invitee = User.objects.get(pk=self.request.data['invitee_id'])
        party = Party.objects.get(pk=self.request.data['party_id'])
        serializer.save(invitee=invitee, party=party)


class InvitationDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (IsHostOrInvitee,)


class HostedPartyList(generics.ListAPIView):
    serializer_class = PartySerializer

    def get_queryset(self):
        host_id = self.kwargs['host_id']
        return Party.objects.filter(host=host_id)


class InvitedPartyList(generics.ListAPIView):
    serializer_class = PartySerializer

    def get_queryset(self):
        invitee_id = self.kwargs['invitee_id']
        """
        note this weird '__in' syntax for filtering on ManyToManyField
        """
        return Party.objects.filter(invitees__in=invitee_id)


class InvitationToPartyList(generics.ListAPIView):
    serializer_class = InvitationSerializer

    def get_queryset(self):
        party_id = self.kwargs['party_id']
        return Invitation.objects.filter(party=party_id)

