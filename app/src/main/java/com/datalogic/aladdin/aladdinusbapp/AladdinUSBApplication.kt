package com.datalogic.aladdin.aladdinusbapp

import android.app.Application
import android.util.Log
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

class AladdinUSBApplication : Application() {

    private lateinit var usbDeviceManager: DatalogicDeviceManager
    private val TAG = AladdinUSBApplication::class.java.simpleName

    override fun onCreate() {
        super.onCreate()

        CommonUtils.initialize(this)

        usbDeviceManager = DatalogicDeviceManager

        // Register for USB events
        usbDeviceManager.registerReceiver(applicationContext)

        // Ensure all devices start in a known state
        usbDeviceManager.resetAllDeviceStates()

        // Set up uncaught exception handler to clean up devices on crash
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // Clean up open devices
            cleanup()

            // Call original handler
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun cleanup() {
        try {
            val openDevices = usbDeviceManager.getDevicesByStatus(DeviceStatus.OPENED)

            for (device in openDevices) {
                device.closeDevice(device.usbDevice)
            }

            // Unregister receiver
            usbDeviceManager.unregisterReceiver(applicationContext)
        } catch (e: Exception) {
            Log.d(TAG, "Error during cleanup")
        }
    }

    override fun onTerminate() {
        cleanup()
        super.onTerminate()
    }
}