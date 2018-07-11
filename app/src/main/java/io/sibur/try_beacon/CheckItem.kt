package io.sibur.try_beacon

import org.altbeacon.beacon.Beacon

interface Factory<out T> {
    fun create(beacons: MutableCollection<Beacon>): T
}

class CheckItem {
    val data = IntArray(3, { -100 })

    companion object : Factory<CheckItem> {
        override fun create(beacons: MutableCollection<Beacon>): CheckItem {
            val item = CheckItem()
            beacons.map {
                when (it.id3.toInt()) {
                    2 -> item.data[0] = it.rssi
                    8 -> item.data[1] = it.rssi
                    9 -> item.data[2] = it.rssi
                }
            }
            return item
        }
    }
}