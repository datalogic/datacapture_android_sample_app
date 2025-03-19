package com.datalogic.aladdin.aladdinusbapp

import android.app.Application
import android.util.Log
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbscannersdk.usbaccess.USBDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbscannersdk.usbaccess.DeviceManager

class AladdinUSBApplication : Application() {

    private lateinit var usbDeviceManager: USBDeviceManager
    private val deviceManager: DeviceManager = DeviceManager()
    private val TAG = AladdinUSBApplication::class.java.simpleName

    override fun onCreate() {
        super.onCreate()

        CommonUtils.initialize(this)

        usbDeviceManager = USBDeviceManager()

        // Register for USB events
        usbDeviceManager.register(applicationContext)

        // Ensure all devices start in a known state
        deviceManager.resetAllDeviceStates()

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
            val openDevices = deviceManager.getDevicesByStatus(DeviceStatus.OPENED)

            for (device in openDevices) {
                usbDeviceManager.closeDevice(device.usbDevice)
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