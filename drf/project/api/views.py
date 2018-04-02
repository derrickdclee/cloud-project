# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from rest_framework import generics

from project.api.models import Party
from project.api.serializers import PartySerializer


class PartyList(generics.ListCreateAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer


class PartyDetail(generics.RetrieveUpdateDestroyAPIView):
    queryset = Party.objects.all()
    serializer_class = PartySerializer
