# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from rest_framework import generics, permissions, status
from rest_framework.response import Response
from rest_framework.decorators import api_view
from rest_framework.reverse import reverse
from rest_framework.parsers import MultiPartParser, FormParser, FileUploadParser
from rest_framework.exceptions import ValidationError
from rest_condition import And, Or, Not
from django.db.models import Q
from django.contrib.auth.models import User
from social_django.models import UserSocialAuth
from geopy import Point, distance

from project.api.permissions import *
from project.api.models import *
from project.api.serializers import *


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


def convert_mile_to_kilometer(mile):
    return mile * 1.609344


class PartyList(generics.ListCreateAPIView):
    queryset = Party.objects.all()
    permission_classes = (permissions.IsAuthenticated,)
    parser_classes = (MultiPartParser, FormParser, FileUploadParser)

    def get_serializer_class(self):
        if self.request.method == 'GET':
            return PartySummarySerializer
        else:
            return PartyManagerSerializer

    def perform_create(self, serializer):
        serializer.save(host=self.request.user)

    def get_queryset(self):
        if not self.request.query_params:
            return Party.objects.all()
        else:
            user = self.request.user.id
            lat = self.request.query_params.get('lat')
            lng = self.request.query_params.get('lng')
            if lat is None or lng is None:
                return Party.objects.none()

            d = self.request.query_params.get('d')
            if d is None:
                d = 10.0
            d = convert_mile_to_kilometer(float(d))

            origin = Point(lat, lng)
            north = distance.VincentyDistance(kilometers=d).destination(origin, 0)
            east = distance.VincentyDistance(kilometers=d).destination(origin, 90)
            south = distance.VincentyDistance(kilometers=d).destination(origin, 180)
            west = distance.VincentyDistance(kilometers=d).destination(origin, 270)

            north_lat = north.latitude
            east_lng = east.longitude
            south_lat = south.latitude
            west_lng = west.longitude

            q = Party.objects\
                .filter(lat__gte=south_lat, lat__lte=north_lat)\
                .exclude(host__id=user).exclude(bouncers__id=user).exclude(invitees__id=user)
            # edge case around the anti-meridian
            if west_lng > east_lng:
                # it seems like you can't chain Q-object queries with regular queries
                return q.filter(Q(lng__gte=west_lng) | Q(lng__lte=east_lng))
            else:
                return q.filter(lng__gte=west_lng, lng__lte=east_lng)


class PartyDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Party.objects.all()
    permission_classes = (Or(IsAdmin, And(IsAuth, IsHostOfPartyObjectOrReadOnly)), )

    def get_serializer_class(self):
        if self.request.method == 'GET':
            party_id = self.kwargs['pk']
            party = Party.objects.get(pk=party_id)
            user = self.request.user
            if user.is_staff or user == party.host:
                return PartyManagerSerializer
            elif user in party.bouncers.all() or user in party.invitees.all():
                return PartySerializer
            else:
                return PartySummarySerializer
        else:
            return PartyManagerSerializer


class AddBouncer(generics.UpdateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartyManagerSerializer
    permission_classes = (Or(IsAdmin, IsHostOfPartyObject),)

    def put(self, request, *args, **kwargs):
        try:
            party = Party.objects.get(pk=kwargs['pk'])
        except Party.DoesNotExist:
            raise ValidationError("The party does not exist.")

        self.check_object_permissions(request, party)

        bouncer_facebook_id = request.data.get('bouncer_facebook_id')
        if bouncer_facebook_id is None:
            raise ValidationError("'bouncer_facebook_id' was not provided.")
        bouncer = lookup_user_with_facebook_id(bouncer_facebook_id)
        if bouncer not in party.invitees.all():
            raise ValidationError("You must invite this user first before adding them as a bouncer.")
        invitation = Invitation.objects.get(party=party, invitee=bouncer)
        if not invitation.has_rsvped:
            raise ValidationError("This invitee has not RSVPed yet.")
        invitation.delete()
        party.bouncers.add(bouncer)  # no need to call save
        serializer = PartyManagerSerializer(party)  # this is serialization, NOT deserialization

        return Response(serializer.data, status=status.HTTP_200_OK)


class RequestToJoinParty(generics.UpdateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartyManagerSerializer
    permission_classes = (permissions.IsAuthenticated, )

    def put(self, request, *args, **kwargs):
        try:
            party = Party.objects.get(pk=kwargs['pk'])
        except Party.DoesNotExist:
            raise ValidationError("The party does not exist.")

        if request.user in party.invitees.all():
            raise ValidationError("You've already been invited.")
        party.requesters.add(request.user)

        return Response(status=status.HTTP_200_OK)


class RejectRequestToJoinParty(generics.UpdateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartyManagerSerializer
    permission_classes = (Or(IsAdmin, IsHostOfPartyObject),)

    def put(self, request, *args, **kwargs):
        try:
            party = Party.objects.get(pk=kwargs['pk'])
        except Party.DoesNotExist:
            raise ValidationError("The party does not exist.")

        self.check_object_permissions(request, party)

        reject_facebook_id = request.data.get('reject_facebook_id')
        if reject_facebook_id is None:
            raise ValidationError("'reject_facebook_id' was not provided.")
        reject = lookup_user_with_facebook_id(reject_facebook_id)
        if reject not in party.requesters.all():
            raise ValidationError("This user did not request to join the party.")
        party.requesters.remove(reject)
        serializer = PartyManagerSerializer(party)

        return Response(serializer.data, status=status.HTTP_200_OK)


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
    # this is easier than overriding the entire post method
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
        if len(potential_conflict) > 0:
            raise ValidationError("The invitee, party pair exists already.")

        serializer.save(invitee=invitee, party=party)


class GrantRequestToJoinParty(generics.CreateAPIView):
    queryset = Invitation.objects.all()
    serializer_class = InvitationSerializer
    permission_classes = (Or(permissions.IsAdminUser, IsHostOfPartyWithParam), )

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
        if len(potential_conflict) > 0:
            raise ValidationError("The invitee, party pair exists already.")

        if invitee not in party.requesters.all():
            raise ValidationError("There was no request by this user.")
        party.requesters.remove(invitee)

        serializer.save(invitee=invitee, party=party)


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

        if invitation.has_rsvped:
            raise ValidationError("You have already RSVPed.")

        invitation.has_rsvped = True
        invitation.save()  # need to explicitly call save here
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

        if not invitation.has_rsvped:
            raise ValidationError("The user has not RSVPed yet.")
        if invitation.has_checkedin:
            raise ValidationError("The user has already checked in.")

        invitation.has_checkedin = True
        invitation.save()  # need to explicitly call save here
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


class BouncingPartyList(generics.ListAPIView):
    serializer_class = PartySummarySerializer
    permission_classes = (Or(permissions.IsAdminUser, IsSameUserWithURLParam),)

    def get_queryset(self):
        bouncer_id = self.kwargs['user_id']
        """
        note this weird double underscore syntax for filtering on ManyToManyField
        """
        return Party.objects.filter(bouncers__id=bouncer_id)


class MyBouncingPartyList(BouncingPartyList):
    permission_classes = (permissions.IsAuthenticated,)

    def get_queryset(self):
        bouncer_id = self.request.user.id
        return Party.objects.filter(bouncers__id=bouncer_id)


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
