from .base import *

DEBUG = False

ALLOWED_HOSTS = [".elasticbeanstalk.com"]
SECRET_KEY = os.environ.get('SECRET_KEY')

SOCIAL_AUTH_FACEBOOK_KEY = os.environ.get('SOCIAL_AUTH_FACEBOOK_KEY')
SOCIAL_AUTH_FACEBOOK_SECRET = os.environ.get('SOCIAL_AUTH_FACEBOOK_SECRET')


# AWS S3 stuff
AWS_STORAGE_BUCKET_NAME = os.environ.get('AWS_STORAGE_BUCKET_NAME')
AWS_ACCESS_KEY_ID = os.environ.get('AWS_ACCESS_KEY_ID')
AWS_SECRET_ACCESS_KEY = os.environ.get('AWS_SECRET_ACCESS_KEY')

AWS_S3_CUSTOM_DOMAIN = '{}.s3.amazonaws.com'.format(AWS_STORAGE_BUCKET_NAME)
AWS_S3_HOST = 's3.us-west-2.amazonaws.com'
AWS_QUERYSTRING_AUTH = False

# static files setting
STATICFILES_LOCATION = 'static'
STATICFILES_STORAGE = 'config.settings.custom_storages.StaticStorage'
STATIC_URL = 'https://{}/{}/'.format(AWS_S3_CUSTOM_DOMAIN, STATICFILES_LOCATION)

# media files setting
MEDIAFILES_LOCATION = 'media'
MEDIA_URL = 'https://{}/{}/'.format(AWS_S3_CUSTOM_DOMAIN, MEDIAFILES_LOCATION)
DEFAULT_FILE_STORAGE = 'config.settings.custom_storages.MediaStorage'
