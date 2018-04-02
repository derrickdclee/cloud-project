# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models

# Create your models here.
class Party(models.Model):
    name = models.CharField(max_length=100)
    description = models.TextField()
    location = models.CharField(max_length=100)
    start_time = models.DateTimeField()
    end_time = models.DateTimeField()
    deleted = models.BooleanField(default=False)

    class Meta:
        verbose_name_plural = 'Parties'

# class Invitation(models.Model):
#     # user = models.ForeignKey(
#     #     ''
#     # )