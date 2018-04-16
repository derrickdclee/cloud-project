import datetime
import os


def set_filename_format(now, instance, filename):
    return "{host_id}-{date}-{microsecond}{extension}".\
        format(host_id=instance.host.id, date=str(now.date()),
               microsecond=now.microsecond, extension=os.path.splitext(filename)[1])


def party_directory_path(instance, filename):
    now = datetime.datetime.now()
    path = "images/{year}/{month}/{day}/{host_id}/{filename}".\
        format(year=now.year, month=now.month, day=now.day,
               host_id=instance.host.id, filename=set_filename_format(now, instance, filename))
    return path
