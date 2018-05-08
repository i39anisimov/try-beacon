package io.sibur.try_beacon

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.sibur.try_beacon.helpers.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.btnReset
import kotlinx.android.synthetic.main.activity_main.btnStart
import kotlinx.android.synthetic.main.activity_main.btnStop

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PermissionHelper.checkPermissionsForMap(this)

        btnStart.setOnClickListener({
            it.isEnabled = false
            startService(Intent(this, BeaconsService::class.java))
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

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this, BeaconsService::class.java))
    }

}
