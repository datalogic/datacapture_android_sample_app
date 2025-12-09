package com.datalogic.aladdin.aladdinusbapp.views.activity

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbDevice
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.AboutModel
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.theme.AladdinUSBAppTheme
import com.datalogic.aladdin.aladdinusbscannersdk.BuildConfig
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.model.LabelCodeType
import com.datalogic.aladdin.aladdinusbscannersdk.model.LabelIDControl
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.CLAIM_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.ENABLE_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.OPEN_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.BluetoothListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.StatusListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbListener
import java.io.File
import kotlin.String

val LocalHomeViewModel = staticCompositionLocalOf<HomeViewModel> {
    error("No HomeViewModel provided")
}

class HomeActivity : AppCompatActivity() {

    private var TAG = HomeActivity::class.java.simpleName
    private lateinit var usbDeviceManager: DatalogicDeviceManager
    private var usbListener: UsbListener? = null
    private var bluetoohListener: BluetoothListener? = null
    private var statusListener: StatusListener? = null
    private val homeViewModel: HomeViewModel by viewModels {
        MyViewModelFactory(usbDeviceManager, applicationContext, this)
    }

    class MyViewModelFactory(
        private val usbDeviceManager: DatalogicDeviceManager,
        private val context: Context,
        private val activity: HomeActivity
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(usbDeviceManager, context, activity) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        handlerUsbListener()
        handlerBluetoothListener(this)
        setContent {
            AladdinUSBAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    CompositionLocalProvider(
                        LocalHomeViewModel provides homeViewModel
                    ) {
                        Navigation()
                    }
                }
            }
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

        Log.d(TAG, "UI falls under: " + (resources.getDimension(R.dimen.test) / resources.displayMetrics.density).toInt())
        Log.d(TAG, "UI screen dimension: " + CommonUtils.getScreenResolution(this))

        // Initialize logging state
        homeViewModel.initializeLoggingState()
        homeViewModel.initializeConnectTypeState()
        homeViewModel.getAppInfo()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // Function to show Toast on the main thread
    fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        Log.d(TAG, "Resume called")
        super.onResume()
        homeViewModel.detectDevice()
        // homeViewModel.appInForeground()
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
        //homeViewModel.appInBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Destroy called")
        homeViewModel.onCleared()
        usbListener?.let { usbDeviceManager.unregisterUsbListener(it) }
        bluetoohListener?.let { usbDeviceManager.unregisterBluetoothListener(it) }
        statusListener?.let { usbDeviceManager.unregisterStatusListener(it) }
    }

    fun handlerUsbListener() {
        usbDeviceManager = DatalogicDeviceManager
        usbListener = object : UsbListener {
            override fun onDeviceAttachedListener(device: UsbDevice) {
                homeViewModel.setDeviceStatus("Attached ${device.productName}")
                homeViewModel.detectDevice()
                homeViewModel.deviceReAttached(device)
            }

            override fun onDeviceDetachedListener(device: UsbDevice) {
                homeViewModel.setDeviceStatus("Detached ${device.productName}")
                homeViewModel.handleDeviceDisconnection(device)
            }
        }
        usbListener?.let {
            usbDeviceManager.registerUsbListener(it)
        }

        // USB connection status listener implementation
        val statusListener = object : StatusListener {
            override fun onStatus(productId: String, status: DeviceStatus, deviceName: String) {
                runOnUiThread {
                    homeViewModel.setStatus(productId, status)
                    Log.d(TAG, "[handlerUsbListener] ${status.name}")

                    // Update UI based on new status
                    when (status) {
                        DeviceStatus.OPENED -> {
                            homeViewModel.onOpenDeviceSuccessResultAction(productId)
                            //Setup listener
                            homeViewModel.setupCustomListeners(homeViewModel.deviceList.value?.firstOrNull {it.usbDevice.deviceName == deviceName})
                            showToast(applicationContext, "Device successfully opened")
                            homeViewModel.setAutoCheckDocking(homeViewModel.isCheckDockingEnabled.value == true)
                        }
                        DeviceStatus.CLOSED -> {
                            showToast(applicationContext, "Device closed (Path: $deviceName)")
                            homeViewModel.clearConfig()
                            homeViewModel.clearSelectedDevice(deviceName)
                            homeViewModel.setSelectedLabelIDControl(LabelIDControl.DISABLE, "")
                            homeViewModel.setSelectedLabelCodeType(LabelCodeType.NONE, "")
                        }
                        else -> {}
                    }
                }
            }

            override fun onError(errorStatus: Int) {
                runOnUiThread {
                    when (errorStatus) {
                        OPEN_FAILURE -> showToast(applicationContext, "Failed to open device")
                        CLAIM_FAILURE -> showToast(applicationContext, "Failed to claim interface")
                        ENABLE_FAILURE -> showToast(applicationContext, "Failed to enable scanner")
                        else -> showToast(applicationContext, "Error: $errorStatus")
                    }
                }
            }
        }
        usbDeviceManager.registerStatusListener(statusListener)
    }

    fun handlerBluetoothListener(activity: HomeActivity) {
        usbDeviceManager = DatalogicDeviceManager
        bluetoohListener = object : BluetoothListener {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onDeviceAttachedListener(device: BluetoothDevice) {
                if (homeViewModel.status.value != DeviceStatus.OPENED) {
                    homeViewModel.setDeviceStatus("Attached ${device.name}")
                }
                homeViewModel.getAllBluetoothDevice(activity)
            }
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onDeviceDetachedListener(device: BluetoothDevice) {
                homeViewModel.deviceList
                homeViewModel.handleBluetoothDeviceDisconnection(device)
            }
        }
        bluetoohListener?.let {
            usbDeviceManager.registerBluetoothListener(it)
        }
    }
}
