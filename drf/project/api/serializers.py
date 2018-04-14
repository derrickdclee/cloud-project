from django.contrib.auth.models import User
from project.api.models import Party, Invitation
from rest_framework import serializers


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'username', 'email', )


class PartySerializer(serializers.ModelSerializer):
    class Meta:
        model = Party
        fields = ('id', 'host', 'bouncers', 'invitees', 'name', 'description', 'image', 'lat', 'lng',
                  'start_time', 'end_time', 'deleted',)

    id = serializers.ReadOnlyField()
    host = serializers.ReadOnlyField(source='host.username')
    bouncers = UserSerializer(many=True, read_only=True)  # read_only required for nested serializer
    invitees = UserSerializer(many=True, read_only=True)
    image = serializers.ImageField(use_url=True)

    def validate(self, data):
        if data['start_time'] > data['end_time']:
            raise serializers.ValidationError("Start time cannot be later than end time.")
        return data


class InvitationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Invitation
        fields = ('id', 'invitee', 'party', 'has_rsvped', 'has_checkedin',)

    id = serializers.ReadOnlyField()
    invitee = serializers.ReadOnlyField(source='invitee.username')
    party = serializers.ReadOnlyField(source='party.name')
