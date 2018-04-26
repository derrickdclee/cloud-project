from django.conf.urls import url
from rest_framework.urlpatterns import format_suffix_patterns
from project.api import views

urlpatterns = [
    url(r'^$', views.api_root),
    url(r'^parties/$', views.PartyList.as_view(), name='party-list'),
    url(r'^parties/(?P<pk>[0-9]+)/$', views.PartyDetail.as_view()),
    url(r'^parties/(?P<pk>[0-9]+)/bouncers/$', views.AddBouncer.as_view()),
    url(r'^parties/(?P<pk>[0-9]+)/request/$', views.RequestToJoinParty.as_view()),
    url(r'^parties/(?P<pk>[0-9]+)/request/reject/$', views.RejectRequestToJoinParty.as_view()),
    url(r'^users/$', views.UserList.as_view(), name='user-list'),
    url(r'^users/(?P<pk>[0-9]+)/$', views.UserDetail.as_view()),
    url(r'^users/me/$', views.MyUserDetail.as_view()),
    url(r'^invitations/$', views.InvitationList.as_view(), name='invitation-list'),
    url(r'^invitations/by-request/$', views.GrantRequestToJoinParty.as_view()),
    url(r'^invitations/(?P<pk>[0-9a-f]{8}\-[0-9a-f]{4}\-4[0-9a-f]{3}\-[89ab][0-9a-f]{3}\-[0-9a-f]{12})/$',
        views.InvitationDetail.as_view()),
    url(r'^invitations/(?P<pk>[0-9a-f]{8}\-[0-9a-f]{4}\-4[0-9a-f]{3}\-[89ab][0-9a-f]{3}\-[0-9a-f]{12})/rsvp/$',
        views.InvitationRsvp.as_view()),
    url(r'^invitations/(?P<pk>[0-9a-f]{8}\-[0-9a-f]{4}\-4[0-9a-f]{3}\-[89ab][0-9a-f]{3}\-[0-9a-f]{12})/checkin/$',
        views.InvitationCheckin.as_view()),
    url(r'^parties/hosted/(?P<user_id>[0-9]+)/$', views.HostedPartyList.as_view()),
    url(r'^parties/hosted/me/$', views.MyHostedPartyList.as_view()),
    url(r'^parties/invited/(?P<user_id>[0-9]+)/$', views.InvitedPartyList.as_view()),
    url(r'^parties/invited/me/$', views.MyInvitedPartyList.as_view()),
    url(r'^parties/bouncing/(?P<user_id>[0-9]+)/$', views.BouncingPartyList.as_view()),
    url(r'^parties/bouncing/me/$', views.MyBouncingPartyList.as_view()),
    url(r'^invitations/to-party/(?P<party_id>[0-9]+)/$', views.InvitationToPartyList.as_view()),
    url(r'^invitations/to-party/(?P<party_id>[0-9]+)/of-user/me/$', views.MyInvitationToPartyDetail.as_view()),
]


urlpatterns = format_suffix_patterns(urlpatterns)
