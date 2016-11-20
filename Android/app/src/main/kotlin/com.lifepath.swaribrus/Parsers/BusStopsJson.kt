package com.lifepath.swaribrus.Parsers

import argo.jdom.JdomParser
import java.util.*

class BusStopsJson {
    companion object {
        val parser = JdomParser()
    }

    fun parse(text: String): BusStops {
        val rootNode = parser.parse(text)

        val items = ArrayList<BusStop>()
        rootNode.getArrayNode("items").forEach {
            val index = it.getNumberValue("index")
            val latitude = it.getStringValue("latitude")
            val longitude = it.getStringValue("longitude")
            val busName = it.getStringValue("bus_name")

            items.add(BusStop(index.toInt(), latitude, longitude, busName))
        }
        return BusStops(items)
    }
}

data class BusStops(val items: List<BusStop>)

data class BusStop(val index: Int,
                val latitude: String,
                val longitude: String,
                val busName: String)
