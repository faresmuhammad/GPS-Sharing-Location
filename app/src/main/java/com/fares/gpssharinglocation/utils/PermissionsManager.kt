package com.fares.gpssharinglocation.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import com.fares.gpssharinglocation.data.AppPreferences
import javax.inject.Inject

class PermissionsManager @Inject constructor(
    private val pref: AppPreferences
) {

    /**
     * @return true if permission is granted
     */
    fun isPermissionGranted(activity: Activity, permission: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissionResult = ActivityCompat.checkSelfPermission(activity, permission)

            if (permissionResult == PackageManager.PERMISSION_GRANTED) {
                return true
            }

        }
        return false
    }

    private fun requestPermission(activity: Activity, requestCode: Int, permissions: String) {
        ActivityCompat.requestPermissions(activity, arrayOf(permissions), requestCode)
    }

    fun handleRequestPermissionsCases(
        activity: Activity,
        permission: String,
        requestCode: Int,
        explanation: () -> Unit
    ) {
        val requestPermissionRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

        if (requestPermissionRationale) {
            explanation()
            requestPermission(activity, requestCode, permission)

        } else {
            if (pref.getIsPermissionFirstAsked(permission)) {
                requestPermission(activity, requestCode, permission)
                pref.setIsPermissionFirstAsked(false, permission)
            } else {
                goToPermissionsSettings(activity)
            }

        }

    }

    private fun goToPermissionsSettings(activity: Activity) {
        val uri = Uri.fromParts("package", activity.packageName, null)
        val permissionSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = uri
            addCategory(Intent.CATEGORY_DEFAULT)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        activity.startActivity(permissionSettings)
    }


}