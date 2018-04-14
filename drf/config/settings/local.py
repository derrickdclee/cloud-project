from .base import *

DEBUG = True

SECRET_KEY ='54+kk=eo895m371t8myy$zbp(3c!hy5swr%0gmf20ud_2i3d$v'

SOCIAL_AUTH_FACEBOOK_KEY = os.environ.get('SOCIAL_AUTH_FACEBOOK_KEY')
SOCIAL_AUTH_FACEBOOK_SECRET = os.environ.get('SOCIAL_AUTH_FACEBOOK_SECRET')

# Static files (CSS, JavaScript, Images)
# https://docs.djangoproject.com/en/1.11/howto/static-files/
STATIC_ROOT = normpath(join(ROOT_DIR, "www", "static"))
STATIC_URL = '/static/'
STATICFILES_FINDERS = (
    'django.contrib.staticfiles.finders.FileSystemFinder',
    'django.contrib.staticfiles.finders.AppDirectoriesFinder',
)

# Media files
MEDIA_ROOT = normpath(join(APPS_DIR, 'media'))
MEDIA_URL = '/media/'
