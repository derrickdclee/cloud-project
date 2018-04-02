from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from project.api import views

urlpatterns = [
    url(r'^parties/$', views.PartyList.as_view()),
    url(r'^parties/(?P<pk>[0-9]+)/$', views.PartyDetail.as_view()),
]

urlpatterns = format_suffix_patterns(urlpatterns)