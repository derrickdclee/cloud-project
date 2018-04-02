from django.contrib.auth.models import User, Group
from project.api.models import Party
from rest_framework import serializers

class PartySerializer(serializers.ModelSerializer):
    class Meta:
        model = Party
        host = serializers.ReadOnlyField(source='host.username')
        fields = ('name', 'description', 'location', 'start_time',
                  'end_time', 'deleted', 'host')


class UserSerializer(serializers.HyperlinkedModelSerializer):
    parties = serializers.PrimaryKeyRelatedField(many=True, queryset=Party.objects.all())

    class Meta:
        model = User
        fields = ('url', 'username', 'email', 'parties')
#
# class GroupSerializer(serializers.HyperlinkedModelSerializer):
#     class Meta:
#         model = Group
#         fields = ('url', 'name')


