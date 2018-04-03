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
]

"""
GET parties/<party_id>/invitations/ -> returns a list of invitations to the party
POST parties/<party_id>/invitations/ -> create a new invitation, with userid is passed as a param
GET invitations/<invitation_id> -> retrieve an invitation
PUT invitations/<invitation_id> -> update an invitation
DELETE invitations/<invitation_id> -> delete an invitation
"""

urlpatterns = format_suffix_patterns(urlpatterns)