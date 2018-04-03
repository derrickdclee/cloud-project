from django.contrib.auth.models import User
from project.api.models import Party, Invitation
from rest_framework import serializers


class PartySerializer(serializers.ModelSerializer):
    host = serializers.ReadOnlyField(source='host.username')
    invitations = serializers.PrimaryKeyRelatedField(many=True, queryset=Invitation.objects.all())

    class Meta:
        model = Party
        fields = ('name', 'description', 'location', 'start_time',
                  'end_time', 'deleted', 'host', 'invitations',)


class UserSerializer(serializers.ModelSerializer):
    parties = serializers.PrimaryKeyRelatedField(many=True, queryset=Party.objects.all())

    class Meta:
        model = User
        fields = ('username', 'email', 'parties',)


class InvitationSerializer(serializers.ModelSerializer):
    invitee = serializers.ReadOnlyField(source='invitee.username')
    party = serializers.ReadOnlyField(source='party.name')

    class Meta:
        model = Invitation
        fields = ('invitee', 'party', 'has_rsvped', 'has_checkedin',)


