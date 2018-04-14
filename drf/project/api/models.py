# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models
from django.dispatch import receiver
from project.api import file_util
import uuid


class Party(models.Model):
    class Meta:
        verbose_name_plural = 'Parties'

    host = models.ForeignKey('auth.User', related_name='parties', on_delete=models.CASCADE)
    # need the related_name fields to be present in bouncers, invitees
    bouncers = models.ManyToManyField('auth.User', related_name='bouncer_parties')
    invitees = models.ManyToManyField('auth.User', related_name='invitee_parties', through='Invitation')
    name = models.CharField(max_length=100)
    description = models.TextField()
    image = models.ImageField(upload_to=file_util.party_directory_path, blank=True, null=True)
    lat = models.DecimalField(max_digits=9, decimal_places=6, null=True)
    lng = models.DecimalField(max_digits=9, decimal_places=6, null=True)
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    deleted = models.BooleanField(default=False)

    def __str__(self):
        return self.name


@receiver(models.signals.post_delete, sender=Party)
def auto_delete_file_on_delete(sender, instance, **kwargs):
    if instance.image:
        instance.image.delete(save=False)


class Invitation(models.Model):
    class Meta:
        unique_together = ("invitee", "party")

    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    invitee = models.ForeignKey('auth.User', related_name='invitations', on_delete=models.CASCADE, editable=False)
    party = models.ForeignKey('Party', related_name='invitations', on_delete=models.CASCADE, editable=False)
    has_rsvped = models.BooleanField(default=False)
    has_checkedin = models.BooleanField(default=False)

    def __str__(self):
        return "{0} has been invited to {1}".format(self.invitee, self.party)
