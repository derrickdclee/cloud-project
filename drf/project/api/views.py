# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from rest_framework import generics, permissions, status
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework.reverse import reverse
from rest_framework.parsers import MultiPartParser, FormParser, FileUploadParser
from rest_framework.exceptions import ValidationError
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


def lookup_user_with_facebook_id(facebook_id):
    try:
        user_fb = UserSocialAuth.objects.get(uid=facebook_id)
    except UserSocialAuth.DoesNotExist:
        raise ValidationError("The user does not exist.")
    return user_fb.user


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
        bouncer_id = request.data.get('bouncer_id')
        if bouncer_id is None:
            raise ValidationError("'bouncer_id' was not provided.")
        bouncer = User.objects.get(pk=bouncer_id)
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
        invitee_facebook_id = self.request.data.get('invitee_facebook_id')
        if invitee_facebook_id is None:
            raise ValidationError("'invitee_facebook_id' was not provided.")
        invitee = lookup_user_with_facebook_id(invitee_facebook_id)

        party_id = self.request.data.get('party_id')
        if party_id is None:
            raise ValidationError("'party_id' was not provided.")
        try:
            party = Party.objects.get(pk=party_id)
        except Party.DoesNotExist:
            raise ValidationError("The party does not exist.")
        serializer.save(invitee=invitee, party=party, facebook_id=invitee_facebook_id)


class InvitationDetail(generics.RetrieveDestroyAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(IsAdmin, Or(IsHostOfInvitationObject, IsInviteeOfInvitationObject)),)


class InvitationRsvp(generics.UpdateAPIView):
    permission_classes = (Or(IsAdmin, IsInviteeOfInvitationObject),)

    def put(self, request, *args, **kwargs):
        try:
            invitation = Invitation.objects.get(pk=kwargs['pk'])
        except Invitation.DoesNotExist:
            raise ValidationError("The invitation does not exist.")

        # we need to explicitly call this as we are overriding the put method
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
        try:
            invitation = Invitation.objects.get(pk=kwargs['pk'])
        except Invitation.DoesNotExist:
            raise ValidationError("The invitation does not exist.")
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


class MyInvitationToPartyDetail(generics.RetrieveAPIView):
    serializer_class = InvitationSerializer
    permission_classes = (permissions.IsAuthenticated, )

    def get_object(self):
        party_id = self.kwargs['party_id']
        invitee_id = self.request.user.id
        try:
            invitation = Invitation.objects.get(party_id=party_id, invitee_id=invitee_id)
        except Invitation.DoesNotExist:
            raise ValidationError("Invalid party_id, invitee_id pair.")
        return invitation
