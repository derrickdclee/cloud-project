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
from django.contrib.auth.models import User
from social_django.models import UserSocialAuth

from project.api.permissions import *
from project.api.models import Party, Invitation
from project.api.serializers import PartySerializer, PartySummarySerializer, UserSerializer, InvitationSerializer


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


def lookup_facebook_id(user):
    return user.social_auth.get(provider='facebook').uid


class PartyList(generics.ListCreateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    permission_classes = (permissions.IsAuthenticated,)
    parser_classes = (MultiPartParser, FormParser, FileUploadParser)

    def get_serializer_class(self):
        if self.request.method == 'GET':
            return PartySummarySerializer
        else:
            return PartySerializer

    def perform_create(self, serializer):
        serializer.save(host=self.request.user)


class PartyDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
    # TODO: should I add IsAuthenticated here?
    permission_classes = (Or(IsAdmin, IsHostOfPartyObjectOrReadOnly), )

    def get_serializer_class(self):
        # for k, v in self.__dict__.items():
        #     print("{0}: {1}".format(k, v))
        if self.request.method == 'GET':
            party_id = self.kwargs['pk']
            party = Party.objects.get(pk=party_id)
            user = self.request.user
            if user.is_staff or user == party.host or user in party.bouncers.all() or user in party.invitees.all():
                return PartySerializer
            else:
                return PartySummarySerializer
        else:
            return PartySerializer


class AddBouncer(APIView):
    permission_classes = (Or(permissions.IsAdminUser, IsHostOfPartyWithURLParam),)

    def put(self, request, *args, **kwargs):
        try:
            party = Party.objects.get(pk=kwargs['pk'])
        except Party.DoesNotExist:
            raise ValidationError("The party does not exist.")

        bouncer_facebook_id = request.data.get('bouncer_facebook_id')
        if bouncer_facebook_id is None:
            raise ValidationError("'bouncer_id' was not provided.")

        bouncer = lookup_user_with_facebook_id(bouncer_facebook_id)
        party.bouncers.add(bouncer)
        serializer = PartySerializer(party)
        return Response(serializer.data, status=status.HTTP_201_CREATED)


class RequestToJoinParty(APIView):
    permission_classes = (permissions.IsAuthenticated, )

    def put(self, request, *args, **kwargs):
        try:
            party = Party.objects.get(pk=kwargs['pk'])
        except Party.DoesNotExist:
            raise ValidationError("The party does not exist.")

        if request.user in party.invitees.all():
            raise ValidationError("You've already been invited.")

        party.requesters.add(request.user)
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
        party = Party.objects.get(pk=party_id)

        """
        Not the best solution... but to avoid internal server error when unique_together on Invitation model fails
        """
        potential_conflict = Invitation.objects.filter(invitee=invitee, party=party)
        if len(potential_conflict) > 0 :
            raise ValidationError("The invitee, party pair exists already.")

        serializer.save(invitee=invitee, party=party, facebook_id=invitee_facebook_id)


class GrantRequestToJoinParty(generics.CreateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(permissions.IsAdminUser, IsHostOfPartyWithParam), )

    def perform_create(self, serializer):
        invitee_id = self.request.data.get('invitee_id')
        if invitee_id is None:
            raise ValidationError("'invitee_id' was not provided.")
        try:
            invitee = User.objects.get(pk=invitee_id)
        except User.DoesNotExist:
            raise ValidationError("The user does not exist.")

        party_id = self.request.data.get('party_id')
        party = Party.objects.get(pk=party_id)

        """
        Not the best solution... but to avoid internal server error when unique_together on Invitation model fails
        """
        potential_conflict = Invitation.objects.filter(invitee=invitee, party=party)
        if len(potential_conflict) > 0:
            raise ValidationError("The invitee, party pair exists already.")

        if invitee not in party.requesters.all():
            raise ValidationError("There was no request by this user.")
        party.requesters.remove(invitee)
        invitee_facebook_id = lookup_facebook_id(invitee)

        serializer.save(invitee=invitee, party=party, facebook_id=invitee_facebook_id)


class InvitationDetail(generics.RetrieveDestroyAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(IsAdmin, Or(IsHostOfInvitationObject, IsInviteeOfInvitationObject)),)


class InvitationRsvp(generics.UpdateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
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

        if invitation.has_checkedin:
            raise ValidationError("You've already checked in.")
        invitation.has_checkedin = True

        invitation.save()
        serializer = InvitationSerializer(invitation)
        return Response(serializer.data, status=status.HTTP_200_OK)


class HostedPartyList(generics.ListAPIView):
    serializer_class = PartySummarySerializer
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
    serializer_class = PartySummarySerializer
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
    queryset = Invitation.objects.all()
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
