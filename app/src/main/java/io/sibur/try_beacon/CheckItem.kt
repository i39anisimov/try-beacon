package io.sibur.try_beacon

import org.altbeacon.beacon.Beacon

interface Factory<out T> {
    fun create(beacons: MutableCollection<Beacon>): T
}

class CheckItem {
    val data = IntArray(4, { -100 })

    companion object : Factory<CheckItem> {
        override fun create(beacons: MutableCollection<Beacon>): CheckItem {
            val item = CheckItem()
            beacons.map {
                when (it.id3.toInt()) {
                    1 -> item.data[0] = it.rssi
                    4 -> item.data[1] = it.rssi
                    6 -> item.data[2] = it.rssi
                    7 -> item.data[3] = it.rssi
                }
            }
            return item
        }
    }
}