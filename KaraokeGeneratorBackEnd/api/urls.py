from django.conf.urls import patterns, include, url
from rest_framework.routers import DefaultRouter
from . import views

router = DefaultRouter()
router.register(r'song', views.SoundFileViewSet)

urlpatterns = patterns(
    '',
    url(r'^', include(router.urls)),
)
