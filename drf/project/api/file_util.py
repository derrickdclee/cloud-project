import datetime
import os


def set_filename_format(now, instance, filename):
    return "{party_id}-{date}-{microsecond}{extension}".\
        format(party_id=instance.id, date=str(now.date()),
               microsecond=now.microsecond, extension=os.path.splitext(filename)[1])


def party_directory_path(instance, filename):
    now = datetime.datetime.now()
    path = "images/{year}/{month}/{day}/{party_id}/{filename}".\
        format(year=now.year, month=now.month, day=now.day,
               party_id=instance.id, filename=set_filename_format(now, instance, filename))
    return path
