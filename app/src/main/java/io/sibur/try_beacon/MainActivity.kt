package io.sibur.try_beacon

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.sibur.try_beacon.helpers.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.btnReset
import kotlinx.android.synthetic.main.activity_main.btnStart
import kotlinx.android.synthetic.main.activity_main.btnStop

class MainActivity : AppCompatActivity() {
    private val REQUEST_ENABLE_BT = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionHelper.checkPermissionsForMap(this)

        btnStart.setOnClickListener({
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            } else {
                startBTService()
            }
        })


        btnStop.setOnClickListener({
            btnStart.isEnabled = false
            it.isEnabled = false
            stopService(Intent(this, BeaconsService::class.java))
        })

        btnReset.setOnClickListener({
            btnStart.isEnabled = true
            btnStop.isEnabled = true
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK)
            startBTService()
    }

    private fun startBTService() {
        btnStart.isEnabled = false
        startService(Intent(this, BeaconsService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BeaconsService::class.java))
    }

}
