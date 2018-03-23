from .base import *

SECRET_KEY = env('DJANGO_SECRET_KEY', default='54+kk=eo895m371t8myy$zbp(3c!hy5swr%0gmf20ud_2i3d$v')

DEBUG = env.bool('DJANGO_DEBUG', default=True)