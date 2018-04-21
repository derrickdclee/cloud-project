from django.contrib.auth.models import User
from project.api.models import Party, Invitation
from rest_framework import serializers


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'username', 'email', 'full_name')

    full_name = serializers.SerializerMethodField(method_name='get_user_full_name')

    def get_user_full_name(self, obj):
        return obj.get_full_name()


class PartySerializer(serializers.ModelSerializer):
    class Meta:
        model = Party
        fields = ('id', 'host', 'bouncers', 'invitees', 'name', 'description', 'image', 'lat', 'lng',
                  'start_time', 'end_time', 'deleted',)

    id = serializers.ReadOnlyField()
    host = serializers.ReadOnlyField(source='host.full_name')
    bouncers = UserSerializer(many=True, read_only=True)  # read_only required for nested serializer
    invitees = UserSerializer(many=True, read_only=True)
    image = serializers.ImageField(use_url=True, required=False)

    def validate(self, data):
        if data['start_time'] > data['end_time']:
            raise serializers.ValidationError("Start time cannot be later than end time.")
        return data


class InvitationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Invitation
        fields = ('id', 'invitee', 'party', 'facebook_id', 'has_rsvped', 'has_checkedin',)

    id = serializers.ReadOnlyField()
    invitee = serializers.ReadOnlyField(source='invitee.full_name')
    party = serializers.ReadOnlyField(source='party.name')
    facebook_id = serializers.ReadOnlyField()
