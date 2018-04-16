# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework.reverse import reverse
from rest_framework.parsers import MultiPartParser, FormParser, FileUploadParser
from rest_condition import And, Or, Not
from social_django.models import UserSocialAuth

from project.api.permissions import IsAdmin, IsGetRequest, IsSameUserObject, IsSameUserWithURLParam, \
    IsHostOfPartyObject, IsHostOfPartyWithURLParam, IsHostOfPartyWithParam, IsHostOfInvitationObject, \
    IsBouncerOfInvitationObject, IsInviteeOfInvitationObject, IsHostOrBouncer
from project.api.models import Party, Invitation
from django.contrib.auth.models import User
from project.api.serializers import PartySerializer, UserSerializer, InvitationSerializer


@api_view(['GET'])
def api_root(request, format=None):
    return Response({
        'users': reverse('user-list', request=request, format=format),
        'parties': reverse('party-list', request=request, format=format),
        'invitations': reverse('invitation-list', request=request, format=format),
    })


class PartyList(generics.ListCreateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser, And(permissions.IsAuthenticated, Not(IsGetRequest))),)
    parser_classes = (MultiPartParser, FormParser, FileUploadParser)

    def perform_create(self, serializer):
        serializer.save(host=self.request.user)


class PartyDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    permission_classes = (Or(IsAdmin, IsHostOfPartyObject), )


class BouncerList(APIView):
    permission_classes = (Or(permissions.IsAdminUser, IsHostOfPartyWithURLParam),)

    def post(self, request, *args, **kwargs):
        party = Party.objects.get(pk=kwargs['pk'])
        bouncer = User.objects.get(pk=request.data['bouncer_id'])
        party.bouncers.add(bouncer)
        serializer = PartySerializer(party)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


class UserList(generics.ListAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (permissions.IsAdminUser,)


class UserDetail(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserSerializer
    permission_classes = (Or(IsAdmin, IsSameUserObject), )


class MyUserDetail(UserDetail):
    permission_classes = (permissions.IsAuthenticated,)

    def get_object(self):
        user_id = self.request.user.id
        return User.objects.get(pk=user_id)


class InvitationList(generics.ListCreateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(permissions.IsAdminUser, And(IsHostOfPartyWithParam, Not(IsGetRequest))),)

    # this is a hook, called when creating an instance
    # we need to override this method as we are writing to a ReadOnlyField
    def perform_create(self, serializer):
        invitee = self.lookup_user_with_facebook_id(self.request.data['invitee_facebook_id'])
        party = Party.objects.get(pk=self.request.data['party_id'])
        serializer.save(invitee=invitee, party=party, facebook_id=self.request.data['invitee_facebook_id'])

    def lookup_user_with_facebook_id(self, facebook_id):
        user_fb = UserSocialAuth.objects.get(uid=facebook_id)
        return user_fb.user


class InvitationDetail(generics.RetrieveDestroyAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(IsAdmin, Or(IsHostOfInvitationObject, IsInviteeOfInvitationObject)),)


class InvitationRsvp(generics.UpdateAPIView):
    permission_classes = (Or(IsAdmin, IsInviteeOfInvitationObject),)

    def put(self, request, *args, **kwargs):
        invitation = Invitation.objects.get(pk=kwargs['pk'])
        self.check_object_permissions(request, invitation)
        invitation.has_rsvped = True
        invitation.save()
        serializer = InvitationSerializer(invitation)
        return Response(serializer.data, status=status.HTTP_200_OK)


class InvitationCheckin(generics.UpdateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(IsAdmin, Or(IsHostOfInvitationObject, IsBouncerOfInvitationObject)),)

    def put(self, request, *args, **kwargs):
        invitation = Invitation.objects.get(pk=kwargs['pk'])
        self.check_object_permissions(request, invitation)
        invitation.has_checkedin = True
        invitation.save()
        serializer = InvitationSerializer(invitation)
        return Response(serializer.data, status=status.HTTP_200_OK)


class HostedPartyList(generics.ListAPIView):
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser, IsSameUserWithURLParam),)

    def get_queryset(self):
        host_id = self.kwargs['user_id']
        return Party.objects.filter(host=host_id)


class MyHostedPartyList(HostedPartyList):
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        host_id = self.request.user.id
        return Party.objects.filter(host=host_id)


class InvitedPartyList(generics.ListAPIView):
    serializer_class = PartySerializer
    permission_classes = (Or(permissions.IsAdminUser, IsSameUserWithURLParam),)

    def get_queryset(self):
        invitee_id = self.kwargs['user_id']
        """
        note this weird double underscore syntax for filtering on ManyToManyField
        """
        return Party.objects.filter(invitees__id=invitee_id)


class MyInvitedPartyList(InvitedPartyList):
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        invitee_id = self.request.user.id
        return Party.objects.filter(invitees__id=invitee_id)


class InvitationToPartyList(generics.ListAPIView):
    serializer_class = InvitationSerializer
    permission_classes = (Or(permissions.IsAdminUser, IsHostOrBouncer), )

    def get_queryset(self):
        party_id = self.kwargs['party_id']
        return Invitation.objects.filter(party=party_id)
