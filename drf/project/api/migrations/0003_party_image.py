# -*- coding: utf-8 -*-
# Generated by Django 1.11.11 on 2018-04-14 02:00
from __future__ import unicode_literals

from django.db import migrations, models
import project.api.file_util


class Migration(migrations.Migration):

    dependencies = [
        ('api', '0002_auto_20180412_1854'),
    ]

    operations = [
        migrations.AddField(
            model_name='party',
            name='image',
            field=models.ImageField(blank=True, null=True, upload_to=project.api.file_util.party_directory_path),
        ),
    ]
