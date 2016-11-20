from __future__ import unicode_literals

from django.db import models
from datetime import datetime

#from django.contrib.gis.db import models


# Create your models here.
class Bus(models.Model):
	position = models.TextField()
	route = models.TextField()
	trips = models.ManyToManyField('Trip', null = True, blank = True)

class Stop(models.Model):
	position = models.TextField()
	'''
    name  = models.TextField()
    description = models.TextField()
    picture = models.ImageField(upload_to = 'images/', default = 'images/default.jpg')
    price = models.IntegerField()
    date = models.DateTimeField(default = datetime.now)
	'''

class Trip(models.Model):
	position = models.TextField()
	bus_id = models.ForeignKey('Bus', null = True, blank = True)
	state = models.TextField(default = "created")
	datetime = models.DateTimeField(default = datetime.now)
