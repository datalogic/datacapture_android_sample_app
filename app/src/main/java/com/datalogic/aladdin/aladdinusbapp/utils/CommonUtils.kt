package com.datalogic.aladdin.aladdinusbapp.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.usb.UsbDevice
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.USBConstants.REQUEST_CODE_BT

object CommonUtils {
    /**
     *  get current device screen resolution
     **/
    fun getScreenResolution(context: Context): String {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        return "$width,$height"
    }

    /**
     * set type of device
     */

    var isTablet: Boolean = false
    var orientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    fun initialize(context: Context) {
        orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // Allow rotation
    }

    fun getUsbDeviceIndex(dlDeviceList: ArrayList<DatalogicDevice>,
                          usbDeviceList: ArrayList<UsbDevice>): Set<Int> {
        val indices = mutableSetOf<Int>()
        for (usbDevice in usbDeviceList) {
            for (dlDevice in dlDeviceList) {
                if (usbDevice == dlDevice.usbDevice) {
                    indices += usbDeviceList.indexOf(usbDevice)
                }
            }
        }
        return indices
    }

}