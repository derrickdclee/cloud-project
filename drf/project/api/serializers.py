from django.contrib.auth.models import User
from project.api.models import Party, Invitation
from rest_framework import serializers
from django.utils import timezone


class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ('id', 'full_name')

    full_name = serializers.SerializerMethodField(method_name='get_user_full_name')

    def get_user_full_name(self, obj):
        return obj.get_full_name()


class PartySerializer(serializers.ModelSerializer):
    class Meta:
        model = Party
        fields = ('id', 'host', 'bouncers', 'invitees', 'name', 'description', 'image', 'lat', 'lng',
                  'start_time', 'end_time',)

    id = serializers.ReadOnlyField()
    host = UserSerializer(read_only=True)
    bouncers = UserSerializer(many=True, read_only=True)  # read_only required for nested serializer
    invitees = UserSerializer(many=True, read_only=True)
    image = serializers.ImageField(use_url=True, required=False)

    def validate(self, data):
        if data['start_time'] > data['end_time']:
            raise serializers.ValidationError("Start time cannot be later than end time.")
        if data['start_time'] < timezone.now():
            raise serializers.ValidationError("Start time cannot be in the past.")
        return data


class PartySummarySerializer(serializers.ModelSerializer):
    class Meta:
        model = Party
        fields = ('id', 'host', 'name', 'description', 'image', 'lat', 'lng',
                  'start_time', 'end_time',)

    id = serializers.ReadOnlyField()
    host = UserSerializer(read_only=True)
    image = serializers.ImageField(use_url=True, required=False)


class InvitationSerializer(serializers.ModelSerializer):
    class Meta:
        model = Invitation
        fields = ('id', 'invitee', 'party', 'facebook_id', 'has_rsvped', 'has_checkedin',)

    id = serializers.ReadOnlyField()
    invitee = UserSerializer(read_only=True)
    party = serializers.ReadOnlyField(source='party.name')
    facebook_id = serializers.ReadOnlyField()
