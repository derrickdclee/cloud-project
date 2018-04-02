from django.contrib.auth.models import User, Group
from project.api.models import Party
from rest_framework import serializers


class PartySerializer(serializers.ModelSerializer):
    host = serializers.ReadOnlyField(source='host.username')

    class Meta:
        model = Party
        fields = ('name', 'description', 'location', 'start_time',
                  'end_time', 'deleted', 'host',)


class UserSerializer(serializers.ModelSerializer):
    parties = serializers.PrimaryKeyRelatedField(many=True, queryset=Party.objects.all())

    class Meta:
        model = User
        fields = ('username', 'email', 'parties',)
#
# class GroupSerializer(serializers.HyperlinkedModelSerializer):
#     class Meta:
#         model = Group
#         fields = ('url', 'name')


