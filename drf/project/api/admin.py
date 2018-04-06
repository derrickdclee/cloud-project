# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.contrib import admin
from project.api.models import Party, Invitation

# Register your models here.
admin.site.register(Party)
admin.site.register(Invitation)