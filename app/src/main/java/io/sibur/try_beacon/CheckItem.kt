package io.sibur.try_beacon

import org.altbeacon.beacon.Beacon
import java.sql.Timestamp

interface Factory<out T> {
    fun create(beacons: MutableCollection<Beacon>): T
}

class CheckItem(var rssi: Int, var timestamp: Long) {

//    companion object : Factory<CheckItem> {
//        override fun create(beacons: MutableCollection<Beacon>): CheckItem {
//            val item = CheckItem()
//            beacons.map {
//                when (it.id3.toInt()) {
//                    2 -> item.data[0] = it.rssi
//                    8 -> item.data[1] = it.rssi
//                    9 -> item.data[2] = it.rssi
//                }
//            }
//            return item
//        }
//    }
}