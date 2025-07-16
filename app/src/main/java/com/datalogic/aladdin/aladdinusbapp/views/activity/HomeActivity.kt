package com.datalogic.aladdin.aladdinusbapp.views.activity

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.usb.UsbDevice
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.theme.AladdinUSBAppTheme
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.CLAIM_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.ENABLE_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.OPEN_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.StatusListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbListener
import java.io.File

val LocalHomeViewModel = staticCompositionLocalOf<HomeViewModel> {
    error("No HomeViewModel provided")
}

class HomeActivity : AppCompatActivity() {

    private var TAG = HomeActivity::class.java.simpleName
    private lateinit var usbDeviceManager: DatalogicDeviceManager
    private var usbListener: UsbListener? = null
    private lateinit var statusListener: StatusListener
    private val homeViewModel: HomeViewModel by viewModels {
        MyViewModelFactory(usbDeviceManager, applicationContext)
    }

    class MyViewModelFactory(
        private val usbDeviceManager: DatalogicDeviceManager,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(usbDeviceManager, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
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
            override fun onStatus(productId: String, status: DeviceStatus) {
                runOnUiThread {
                    homeViewModel.setStatus(productId, status)

                    // Update UI based on new status
                    when (status) {
                        DeviceStatus.OPENED -> {
                            //Setup listener
                            homeViewModel.setupCustomListeners(homeViewModel.selectedDevice.value)
                            showToast(applicationContext, "Device successfully opened")
                        }
                        DeviceStatus.CLOSED -> {
                            showToast(applicationContext, "Device closed")
                            homeViewModel.clearConfig()
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
        usbDeviceManager.unregisterStatusListener(statusListener)
    }

}
