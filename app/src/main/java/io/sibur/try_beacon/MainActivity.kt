package io.sibur.try_beacon

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
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
        //   RunningAverageRssiFilter.setSampleExpirationMilliseconds(15000L)
        BeaconManager.setAndroidLScanningDisabled(true)
        beaconManager.backgroundBetweenScanPeriod = BETWEEN_SCAN_PERIOD
        beaconManager.backgroundScanPeriod = SCAN_PERIOD


        btnStart.setOnClickListener({
            beaconManager.bind(this)
        })

        btnStop.setOnClickListener({
            beaconManager.stopRangingBeaconsInRegion(region)
            beaconManager.unbind(this)
            btnStart.isEnabled = false
        })

        btnReset.setOnClickListener({
            dataList.clear()
            tvChecksCount.text = "0"
            btnStart.isEnabled = true
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        btnStop.callOnClick()
    }

    override fun onBeaconServiceConnect() {
        beaconManager.addRangeNotifier { beacons, _ ->
            if (beacons.isNotEmpty()) {
                mainThread.post({ addItem(CheckItem.create(beacons)) })
            }
        }
        try {
            beaconManager.startRangingBeaconsInRegion(region)
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
    }

    private fun addItem(item: CheckItem) {
        dataList.add(item)
        tvChecksCount.text = dataList.size.toString()
    }

}
