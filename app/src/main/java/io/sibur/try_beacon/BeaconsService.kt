package io.sibur.try_beacon

import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import org.altbeacon.beacon.BeaconConsumer
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.Region
import org.altbeacon.beacon.service.ArmaRssiFilter
import java.io.File
import java.io.FileWriter
import java.io.IOException

private const val TAG = "try-beacon"
private const val IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
private const val BETWEEN_SCAN_PERIOD = 0L
private const val SCAN_PERIOD = 1100L

class BeaconsService : Service(), BeaconConsumer {

    companion object {
        val region = Region("any", null, null, null)
    }

    private lateinit var beaconManager: BeaconManager
    private val dataList = ArrayList<CheckItem>()

    override fun onCreate() {
        super.onCreate()
        init()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        beaconManager.bind(this)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        beaconManager.stopRangingBeaconsInRegion(BeaconsService.region)
        beaconManager.unbind(this)

        saveToCSV()
        dataList.clear()

        super.onDestroy()
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                addItem(CheckItem.create(beacons))
            }
        }
        try {
            beaconManager.startRangingBeaconsInRegion(BeaconsService.region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun addItem(item: CheckItem) {
        dataList.add(item)
    }

    private fun saveToCSV() {
        var fileWriter: FileWriter? = null
        val CSV_HEADER = "n,1,2,3,4"

        try {
            val file = File(Environment.getExternalStorageDirectory(), "download/" + System.currentTimeMillis().toString() + ".txt")

            fileWriter = FileWriter(file)

            fileWriter.append(CSV_HEADER)
            fileWriter.append('\n')

            var num = 1
            for (check in dataList) {
                fileWriter.append(num.toString())
                fileWriter.append(',')
                fileWriter.append(check.data[0].toString())
                fileWriter.append(',')
                fileWriter.append(check.data[1].toString())
                fileWriter.append(',')
                fileWriter.append(check.data[2].toString())
                fileWriter.append(',')
                fileWriter.append(check.data[3].toString())
                fileWriter.append('\n')
                num++
            }

            Log.d(TAG, "Write CSV successfully!")
        } catch (e: Exception) {
            Log.d(TAG, "Writing CSV error!")
            e.printStackTrace()
        } finally {
            try {
                fileWriter!!.flush()
                fileWriter.close()
            } catch (e: IOException) {
                Log.e(TAG, "Flushing/closing error!")
                e.printStackTrace()
            }
        }
    }

    private fun init() {
        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter::class.java)
        BeaconManager.setAndroidLScanningDisabled(true)
        beaconManager.backgroundBetweenScanPeriod = BETWEEN_SCAN_PERIOD
        beaconManager.backgroundScanPeriod = SCAN_PERIOD
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
