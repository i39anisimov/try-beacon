package io.sibur.try_beacon

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.sibur.try_beacon.helpers.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.btnReset
import kotlinx.android.synthetic.main.activity_main.btnStart
import kotlinx.android.synthetic.main.activity_main.btnStop
import kotlinx.android.synthetic.main.activity_main.tvChecksCount
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

class MainActivity : AppCompatActivity(), BeaconConsumer {
    private lateinit var beaconManager: BeaconManager
    private val mainThread = Handler(Looper.getMainLooper())
    private val dataList = ArrayList<CheckItem>()


    companion object {
        val region = Region("any", null, null, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionHelper.checkPermissionsForMap(this)

        beaconManager = BeaconManager.getInstanceForApplication(this)
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
        BeaconManager.setRssiFilterImplClass(ArmaRssiFilter::class.java)
        BeaconManager.setAndroidLScanningDisabled(true)
        beaconManager.backgroundBetweenScanPeriod = BETWEEN_SCAN_PERIOD
        beaconManager.backgroundScanPeriod = SCAN_PERIOD
        beaconManager.bind(this)

        btnStart.setOnClickListener({
            try {
                beaconManager.startRangingBeaconsInRegion(region)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
            it.isEnabled = false
        })

        btnStop.setOnClickListener({
            beaconManager.stopRangingBeaconsInRegion(region)

            btnStart.isEnabled = false
            it.isEnabled = false
            saveToCSV()
        })

        btnReset.setOnClickListener({
            dataList.clear()
            mainThread.removeCallbacksAndMessages(null)
            tvChecksCount.text = "0"
            btnStart.isEnabled = true
            btnStop.isEnabled = true
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager.unbind(this)
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                mainThread.post({ addItem(CheckItem.create(beacons)) })
            }
        }
    }

    private fun addItem(item: CheckItem) {
        dataList.add(item)
        tvChecksCount.text = dataList.size.toString()
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

}
