package com.datalogic.aladdin.aladdinusbapp

import android.app.Application
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbapp.utils.SharedPreferenceUtil
import com.datalogic.aladdin.aladdinusbscannersdk.usbaccess.USBDeviceManager

class AladdinUSBApplication : Application() {

    private lateinit var usbDeviceManager: USBDeviceManager
    private val TAG = AladdinUSBApplication::class.java.simpleName

    override fun onCreate() {
        super.onCreate()

        CommonUtils.initialize(this)
        SharedPreferenceUtil.initSharedPreference(this)

        usbDeviceManager = USBDeviceManager()
        usbDeviceManager.register(applicationContext)
    }
    override fun onTerminate() {
        super.onTerminate()
        usbDeviceManager.unregisterReceiver(applicationContext)
    }
}