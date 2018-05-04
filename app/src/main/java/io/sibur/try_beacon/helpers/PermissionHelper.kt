package io.sibur.try_beacon.helpers

import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity

private val PERM_WES = "android.permission.WRITE_EXTERNAL_STORAGE"
private val PERM_RES = "android.permission.READ_EXTERNAL_STORAGE"
private val PERM_AFL = "android.permission.ACCESS_FINE_LOCATION"
private val PERM_ACL = "android.permission.ACCESS_COARSE_LOCATION"

class PermissionHelper {
    companion object {
        fun checkPermissionsForMap(context: AppCompatActivity) {
            PermissionHelper().checkPermissions(context, PERM_RES, PERM_WES, PERM_ACL, PERM_AFL)
        }
    }

    private fun checkPermissions(context: AppCompatActivity, vararg params: String): Boolean {
        var answer = true
        val nonGranted = ArrayList<String>()
        for (s in params) {
            val res = context.checkCallingOrSelfPermission(s)
            if (res == PackageManager.PERMISSION_DENIED) {
                nonGranted.add(s)
            }
            answer = answer and (res == PackageManager.PERMISSION_GRANTED)
        }
        if (!answer) {
            context.requestPermissions(
                nonGranted.toArray(arrayOfNulls<String>(nonGranted.size)), 1
            )
        }
        return answer
    }
}