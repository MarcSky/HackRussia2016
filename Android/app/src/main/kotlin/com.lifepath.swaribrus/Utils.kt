package com.lifepath.swaribrus

import android.location.Location

fun chekcLocationIn(point: Location, radius: Int, loc: Location): Boolean {
    val distance = FloatArray(2)

    Location.distanceBetween(loc.latitude, loc.longitude,
            point.latitude, point.longitude, distance)

    if (distance[0] > radius) {
        return false
    } else {
        return true
    }
}
