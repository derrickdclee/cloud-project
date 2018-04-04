from django.contrib.auth.models import User
from project.api.models import Party, Invitation
from rest_framework import serializers


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('username', 'email', )


class PartySerializer(serializers.ModelSerializer):
    host = serializers.ReadOnlyField(source='host.username')
    invitees = UserSerializer(many=True, read_only=True)

    class Meta:
        model = Party
        fields = ('name', 'description', 'location', 'start_time',
                  'end_time', 'deleted', 'host', 'invitees',)


class InvitationSerializer(serializers.ModelSerializer):
    invitee = serializers.ReadOnlyField(source='invitee.username')
    party = serializers.ReadOnlyField(source='party.name')

    class Meta:
        model = Invitation
        fields = ('invitee', 'party', 'has_rsvped', 'has_checkedin',)


