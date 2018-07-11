package io.sibur.try_beacon

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import kotlin.experimental.and

private const val TAG = "try-beacon"
private const val SCAN_PERIOD = 10000L

class BeaconsService : Service() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private val dataList1 = ArrayList<CheckItem>()
    private val dataList2 = ArrayList<CheckItem>()
    private val dataList3 = ArrayList<CheckItem>()
    private val startTimeStamp = System.currentTimeMillis() / 1_000

    private var mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val parsedResult = parseData(result.scanRecord.bytes)
            if (parsedResult.first == 1) {
                addItem(parsedResult.second, result.rssi, System.currentTimeMillis() / 1_000 - startTimeStamp)
                Log.d(TAG, "${parsedResult.first}/${parsedResult.second}/${result.rssi}")
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                onScanResult(-1, sr)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE// onScanFailed. Error Code: $errorCode")
        }


    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val filters = ArrayList<ScanFilter>()

        bluetoothLeScanner.startScan(filters, settings, mScanCallback)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        bluetoothLeScanner.stopScan(mScanCallback)
        saveToCSV(dataList1)
        saveToCSV(dataList2)
        saveToCSV(dataList3)

        super.onDestroy()
    }

    private fun parseData(scanRecord: ByteArray): Pair<Int, Int> {
        var startByte = 2
        var patternFound = false
        var major = 0
        var minor = 0
        while (startByte <= 5) {
            if (scanRecord[startByte + 2] and 0xff.toByte() == 0x02.toByte() &&
                scanRecord[startByte + 3] and 0xff.toByte() == 0x15.toByte()) {
                patternFound = true
                break
            }
            startByte++
        }

        if (patternFound) {
            val uuidBytes = ByteArray(16)
            System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16)
            val hexString = bytesToHex(uuidBytes)

            val uuid = hexString.substring(0, 8) + "-" +
                hexString.substring(8, 12) + "-" +
                hexString.substring(12, 16) + "-" +
                hexString.substring(16, 20) + "-" +
                hexString.substring(20, 32)

            major = (scanRecord[startByte + 20] and 0xff.toByte()) * 0x100 + (scanRecord[startByte + 21] and 0xff.toByte())
            minor = (scanRecord[startByte + 22] and 0xff.toByte()) * 0x100 + (scanRecord[startByte + 23] and 0xff.toByte())
        }
        return Pair(major, minor)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = "0123456789ABCDEF".toCharArray()
        val hexChars = CharArray(bytes.size * 2)
        try {
            for (j in bytes.indices) {
                val v = bytes[j] and 0xFF.toByte()
                hexChars[j * 2] = hexArray[v.toInt().ushr(4)]
                hexChars[j * 2 + 1] = hexArray[v.toInt() and 0x0F]
            }
        } catch (ex: Exception) {
//            ex.printStackTrace()
        }
        return String(hexChars)
    }

    private fun addItem(minor: Int, rssi: Int, timeStamp: Long) {
        when(minor) {
            2 -> dataList1
            8 -> dataList2
            9 -> dataList3
            else -> null
        }?.add(CheckItem(rssi, timeStamp))
    }

    private fun saveToCSV(list: ArrayList<CheckItem>) {
        var fileWriter: FileWriter? = null
        val CSV_HEADER = "time,rssi"

        try {
            val file = File(Environment.getExternalStorageDirectory(), "download/" + System.currentTimeMillis().toString() + ".txt")

            fileWriter = FileWriter(file)

            fileWriter.append(CSV_HEADER)
            fileWriter.append('\n')

//            var num = 1
            for (check in list) {
                fileWriter.append("${check.timestamp},${check.rssi}\n")
//                check.data.map { fileWriter!!.append(", $it") }
//                fileWriter.append('\n')
//                num++
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

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
