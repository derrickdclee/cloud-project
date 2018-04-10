from django.contrib.auth.models import User
from project.api.models import Party, Invitation
from rest_framework import serializers
from rest_framework.validators import UniqueTogetherValidator


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'username', 'email', )


class PartySerializer(serializers.ModelSerializer):
    class Meta:
        model = Party
        fields = ('id', 'host', 'invitees', 'name', 'description', 'location', 'start_time', 'end_time', 'deleted',)

    id = serializers.ReadOnlyField()
    host = serializers.ReadOnlyField(source='host.username')
    invitees = UserSerializer(many=True, read_only=True)

    def validate(self, data):
        if data['start_time'] > data['end_time']:
            raise serializers.ValidationError("Start time cannot be later than end time.")
        return data


class InvitationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Invitation
        fields = ('id', 'invitee', 'party', 'has_rsvped', 'has_checkedin',)
        validators = [
            UniqueTogetherValidator(
                queryset=Invitation.objects.all(),
                fields=('invitee', 'party')
            )
        ]

    id = serializers.ReadOnlyField()
    invitee = serializers.ReadOnlyField(source='invitee.username')
    party = serializers.ReadOnlyField(source='party.name')
