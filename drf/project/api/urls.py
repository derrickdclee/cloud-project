from django.conf.urls import url, include
from project.api import views

urlpatterns = [
    url(r'^parties/$', views.party_list),
    url(r'^parties/(?P<pk>[0-9]+)/$', views.party_detail),
]