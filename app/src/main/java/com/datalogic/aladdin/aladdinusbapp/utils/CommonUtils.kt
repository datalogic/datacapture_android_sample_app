package com.datalogic.aladdin.aladdinusbapp.utils

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager

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
    var orientation: Int = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    fun initialize(context: Context) {
        isTablet = context.resources.configuration.smallestScreenWidthDp >= 600
        if (isTablet) {
            orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            orientation =ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}