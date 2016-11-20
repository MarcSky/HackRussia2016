from django.shortcuts import render
from django.http import HttpResponse, HttpResponseRedirect

import json
from .models import *


def _init(request):
	stops = [map(lambda a: float(a) / 10e4, x.split(';')[1:3]) for x in open('stops.txt').read().split('\n')[1:-1]]
	for s in stops:
		stop = Stop(position = json.dumps({"lat": s[0], "long": s[1]}))
		stop.save()
	bus = Bus(route = '1337')
	bus.save()
	return HttpResponse('{"state": "ok"}')


def _get_distance(llat1,llong1, llat2, llong2):
	import math

	rad = 6372795

	lat1 = llat1*math.pi/180.
	lat2 = llat2*math.pi/180.
	long1 = llong1*math.pi/180.
	long2 = llong2*math.pi/180.

	cl1 = math.cos(lat1)
	cl2 = math.cos(lat2)
	sl1 = math.sin(lat1)
	sl2 = math.sin(lat2)
	delta = long2 - long1
	cdelta = math.cos(delta)
	sdelta = math.sin(delta)

	y = math.sqrt(math.pow(cl2*sdelta,2)+math.pow(cl1*sl2-sl1*cl2*cdelta,2))
	x = sl1*sl2+cl1*cl2*cdelta
	ad = math.atan2(y,x)
	dist = ad*rad
	return dist


def _find_nearest(lat, long):
	return {"bus_id": 1, "lat": 47.22751, "long": 39.71083}


def update_position(request, bus_id, lat, long):
	#json.dump([lat, long]], open('_db/{}'.format(bus_id), 'w'))
	bus = Bus.objects.get(id = bus_id)
	bus.position = {"lat": lat, "long": long}
	bus.save()
	print
	return HttpResponse(len(bus.trips.all()))


def create_trip(request, lat, long, bus_id):
	bus = Bus.objects.get(id = bus_id)
	trip = Trip(position = json.dumps({"lat": lat, "long": long}), bus_id = bus, state = 'assigned')
	trip.save()

	bus.trips.add(trip)
	bus.save()

	position = json.loads(bus.position.replace("u'", "'").replace("'", '"'))
	distance = _get_distance(float(position['lat']), float(position['long']), float(lat), float(long))

	json_trip = {"trip_id": trip.id, "distance": distance}
	return HttpResponse(json.dumps(json_trip))


def delete_trip(request, trip_id):
	trip = Trip.objects.get(id = trip_id)
	trip.delete()
	return HttpResponse('{"state": "ok"}')


def update_trip(request, trip_id, state, bus_id):
	trip = Trip.objects.get(id = trip_id)
	trip.state = state
	bus = Bus.objects.get(id = bus_id)
	trip.bus_id = bus
	trip.save()
	bus.trips.add(trip)
	bus.save()
	return HttpResponse('{"state": "ok"}')


def get_nearest(request, lat, long):
	nearest = _find_nearest(lat, long)
	return HttpResponse(json.dumps(nearest))


def get_bus(request, bus_id):
	bus = Bus.objects.get(id = bus_id)
	print bus.position
	b = bus.position.replace("u'", "'").replace("'", '"')
	print b
	position = json.loads(b)
	pos_json = [position["lat"], position["long"]]
	return HttpResponse(json.dumps(pos_json))


def get_bus_distance(request, bus_id, lat, long):
	bus = Bus.objects.get(id = bus_id)
	position = json.loads(bus.position.replace("u'", "'").replace("'", '"'))
	distance = _get_distance(float(position['lat']), float(position['long']), float(lat), float(long))
	return HttpResponse(distance)


def get_man_position(request):
	trip = Trip.objects.latest('id')
	b = trip.position.replace("u'", "'").replace("'", '"')
	print b
	position = json.loads(b)
	pos_json = [position["lat"], position["long"]]
	return HttpResponse(json.dumps(pos_json))


def index(request):
	return render(request, 'index.html')
