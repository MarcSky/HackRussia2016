"""bus URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/1.10/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  url(r'^$', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  url(r'^$', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.conf.urls import url, include
    2. Add a URL to urlpatterns:  url(r'^blog/', include('blog.urls'))
"""
from django.conf.urls import url
from django.contrib import admin

from bus_app import views

urlpatterns = [
    url(r'^admin/', admin.site.urls),
	url(r'^api/_init', views._init),
	url(r'^$', views.index),
    url(r'^api/update/(?P<bus_id>\d+)/(?P<lat>\d+\.\d+)/(?P<long>\d+\.\d+)', views.update_position),
    url(r'^api/get/(?P<lat>\d+\.\d+)/(?P<long>\d+\.\d+)', views.get_nearest),
    url(r'^api/bus/get/(?P<bus_id>\d+)', views.get_bus),
    url(r'^api/bus/get_distance/(?P<bus_id>\d+)/(?P<lat>\d+\.\d+)/(?P<long>\d+\.\d+)', views.get_bus_distance),
    url(r'^api/trip/create/(?P<lat>\d+\.\d+)/(?P<long>\d+\.\d+)/(?P<bus_id>\d+)', views.create_trip),
    url(r'^api/trip/delete/(?P<trip_id>\d+)', views.delete_trip),
    url(r'^api/trip/update/(?P<trip_id>\d+)/(?P<state>\w+)/(?P<bus_id>\d+)', views.update_trip),
	url(r'^api/get_man_position', views.get_man_position),

]
