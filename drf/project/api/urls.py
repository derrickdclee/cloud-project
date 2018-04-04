from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from project.api import views

urlpatterns = [
    url(r'^parties/$', views.PartyList.as_view()),
    url(r'^parties/(?P<pk>[0-9]+)/$', views.PartyDetail.as_view()),
    url(r'^users/$', views.UserList.as_view()),
    url(r'^users/(?P<pk>[0-9]+)/$', views.UserDetail.as_view()),
    url(r'^invitations/$', views.InvitationList.as_view()),
    url(r'^invitations/(?P<pk>[0-9]+)/$', views.InvitationDetail.as_view()),
    url(r'^parties/hosted/(?P<host_id>[0-9]+)/$', views.HostedPartyList.as_view()),
    # url(r'^parties/invited/(?P<invitee_id>[0-9]+)/$',
    #     views.InvitedPartyList.as_view(),)
]

"""
TODO:
list of parties that a user is hosting
list of parties a user has been invited to

list of people that have been invited to a party


GET parties/hosted?host_id=<user_id>
GET parties/invited?invitee_id=<user_id>

"""

urlpatterns = format_suffix_patterns(urlpatterns)