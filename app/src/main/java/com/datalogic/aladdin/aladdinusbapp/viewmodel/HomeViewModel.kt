package com.datalogic.aladdin.aladdinusbapp.viewmodel

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.datalogic.aladdin.aladdinusbapp.utils.USBConstants
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbDeviceList
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbScanData
import com.datalogic.aladdin.aladdinusbscannersdk.usbaccess.USBDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HomeViewModel(usbDeviceManager: USBDeviceManager, context: Context) : ViewModel() {
    private var usbDeviceManager: USBDeviceManager

    private val _status = MutableLiveData<DeviceStatus>()
    val status: LiveData<DeviceStatus> = _status

    private val _deviceStatus = MutableLiveData<String>()
    val deviceStatus: LiveData<String> = _deviceStatus

    private val _deviceList = MutableLiveData<ArrayList<UsbDeviceList>>(ArrayList())
    val deviceList: LiveData<ArrayList<UsbDeviceList>> = _deviceList

    private val _scanLabel = MutableLiveData("")
    val scanLabel: LiveData<String> = _scanLabel

    private val _scanData = MutableLiveData("")
    val scanData: LiveData<String> = _scanData

    private val selectedDevice: MutableLiveData<UsbDeviceList?> = MutableLiveData(null)

    private val _selectedTabIndex = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    private var TAG: String = HomeViewModel::class.java.simpleName
    private var context: Context


    val usbDeviceStatus: HashMap<String, Pair<DeviceStatus?, Boolean>> = HashMap()
    var comPopup by mutableStateOf(false)
    private var isScanEnable: Boolean = false

    init {
        this.usbDeviceManager = usbDeviceManager
        _status.postValue(DeviceStatus.CLOSED)
        this.context = context
    }

    /**
     *  Function to update the selected tab index
     **/
    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    /** setting the selected device to variable _selectedDevice
     * @param device is the selected device
     */
    fun setSelectedDevice(device: UsbDeviceList?) {
        selectedDevice.value = device
        selectedDevice.value?.let {
            val productId = it.usbDevice.productId.toString()

            setDeviceStatus("Attached ${it.usbDevice.productName}")
            usbDeviceStatus[productId].let {
                it?.let {
                    _status.postValue(it.first!!)
                }
            }
        }
    }


    /** Function to add a new key-value pair to the HashMap if the key does not exist
     * @param map is the Hashmap usbDeviceStatus
     * @param key is the productId as key
     * @param value is the scanner status as value
     */
    fun addKeyValueIfAbsent(
        map: HashMap<String, Pair<DeviceStatus?, Boolean>>,
        key: String,
        value: DeviceStatus?
    ) {
        if (!map.containsKey(key)) {
            map[key] = Pair(value, false)
        }
    }

    /** function to check for the connected devices
     */
    fun checkConnectedDevice() {
        val usbDevices = usbDeviceManager.checkConnectedDevice(context)
        val usbDevicelist = ArrayList<UsbDeviceList>()
        if (usbDevices.isNotEmpty()) {
            for (device in usbDevices) {
                usbDevicelist.add(device)
                addKeyValueIfAbsent(
                    usbDeviceStatus,
                    device.usbDevice.productId.toString(),
                    DeviceStatus.CLOSED
                )
            }
        }
        _deviceList.postValue(usbDevicelist)
    }

    /** function to handle application when the device is detached
     * @param device is the disconnected device
     */
    fun handleDeviceDisconnection(device: UsbDevice) {
        clearScanData()
        usbDeviceManager.handleDeviceDisconnection(device)

        _status.postValue(DeviceStatus.NONE)
        checkConnectedDevice()
    }

    /** function to set device status message
     * @param status is status message
     */
    fun setDeviceStatus(status: String) {
        _deviceStatus.postValue(status)
    }

    /** function to set scanned data
     * @param scannedData is scanned data
     */
    fun setScannedData(scannedData: UsbScanData) {
        _scanData.postValue(scannedData.barcodeData)
        _scanLabel.postValue(scannedData.barcodeType)
    }

    /** function to clear scanned data and label
     */
    fun clearScanData() {
        _scanLabel.postValue("")
        _scanData.postValue("")
    }

    /** function to handle when device is reattached
     * @param device is the reattached device
     */
    fun deviceReAttached(device: UsbDevice) {
//        _deviceReAttached.value = true

        usbDeviceStatus[device.productId.toString()]?.let {
            it.let {
                usbDeviceStatus[device.productId.toString()] = Pair(it.first, true)
                if (it.first == DeviceStatus.ENABLED) {
                    usbDeviceStatus[device.productId.toString()] = Pair(DeviceStatus.CLAIMED, true)
                    _status.postValue(DeviceStatus.CLAIMED)
                }
            }
        }

    }

    /** function to set the device status in Hashmap
     * @param productId is the key
     * @param status is value1
     * @param reattached is value2
     */
    fun setStatus(productId: String, status: DeviceStatus, reattached: Boolean) {
        usbDeviceStatus[productId] = Pair(status, reattached)
        Log.d("ANITHA", "setStatus {$status}")
        _status.postValue(status)
    }


    /**
     * Open the USB connection once permission is granted.
     */
    fun openUsbConnection() {
        if (selectedDevice.value?.deviceType == "OEM") {
            selectedDevice.value?.let { targetDevice ->
                CoroutineScope(Dispatchers.IO).launch {
                    //_isLoading.postValue(true)
                    when (usbDeviceManager.openConnection(targetDevice, context)) {
                        USBConstants.SUCCESS -> {
                            Log.d(
                                TAG,
                                "USB Connection opened for device: ${targetDevice.displayName}"
                            )
                            usbDeviceStatus[targetDevice.usbDevice.productId.toString()] =
                                Pair(DeviceStatus.OPENED, false)
                            _status.postValue(DeviceStatus.OPENED)
                        }

                        USBConstants.USB_CONNECTION_FAILURE -> {
                            Log.e(TAG, "Failed to open USB connection.")
                        }

                        else -> {
                            Log.e(TAG, "No permission to open USB connection.")
                        }
                    }
                }
            }
        } else {
            comPopup = true
        }
    }

    /**
     * function to check and return whether the device is reattached or not
     * @param usbDeviceList is the device to be checked
     * @return true if the device is reattached and false if not
     */
    private fun checkDeviceReattached(usbDeviceList: UsbDeviceList): Boolean {
        usbDeviceStatus[usbDeviceList.usbDevice.productId.toString()]?.let {
            it.let {
                return it.second
            }
        }
        return false
    }

    /**
     * Claim the USB interface after opening the connection.
     */
    fun claim() {

        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
              //  _isLoading.postValue(true)
                val isAttached = checkDeviceReattached(it)
                val response = if (isAttached) {
                    Log.d("ANITHA", "claimed: deviceReConnect")
                    usbDeviceManager.deviceReConnect(it, context)
                } else {
                    Log.d("ANITHA", "claimed: claimUsbInterface")
                    usbDeviceManager.claimUsbInterface(it.usbDevice, context)
                }
                when (response) {
                    USBConstants.SUCCESS -> {
                        Log.d(TAG, "Interface claimed for: ${it.displayName}")

                        _status.postValue(DeviceStatus.CLAIMED)
                        usbDeviceStatus[it.usbDevice.productId.toString()] =
                            Pair(DeviceStatus.CLAIMED, false)
                    }

                    else -> {
                        Log.e(TAG, "Failed to claim interface")
                    }
                }
            }
        }

    }

    /**
     * Close the USB connection.
     */
    fun close(): Boolean {
        selectedDevice.value?.let {
            usbDeviceManager.closeUsbConnection(it.usbDevice)
            setStatus(it.usbDevice.productId.toString(), DeviceStatus.CLOSED, false)
            return true
        }
        return false
    }

    /**
     * Release the USB interface after disabling the device.
     */
    fun release(): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()
        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
               // _isLoading.postValue(true)
                val success = when (usbDeviceManager.releaseUsbInterface(it.usbDevice)) {
                    USBConstants.SUCCESS -> {
                        Log.d(TAG, "Interface released")
                        setStatus(it.usbDevice.productId.toString(), DeviceStatus.RELEASED, true)
                        true
                    }

                    else -> {
                        Log.e(TAG, "Failed to release interface")
                        false
                    }
                }
                deferredResult.complete(success)
            }
        } ?: deferredResult.complete(false)

        // Blocking the current thread to wait for the result
        return runBlocking { deferredResult.await() }
    }

    fun errorHandling() {
        selectedDevice.value?.let {
            usbDeviceManager.handleDeviceDisconnection(it.usbDevice)
            CoroutineScope(Dispatchers.IO).launch {
                Log.d("ANITHA", "errorHandling deviceReConnect")
                val response = usbDeviceManager.deviceReConnect(it, context)
                if (response == AladdinConstants.SUCCESS) {
                    Log.d("ANITHA", "errorHandling Sucess")
                }
            }
        }
    }

    /**
     * Enable the Connected Scanner.
     */
    fun enabled() {
        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                //_isLoading.postValue(true)
                val response = if (checkDeviceReattached(it)) {
                    Log.d("ANITHA", "enabled deviceReConnect")
                    usbDeviceManager.deviceReConnect(it, context)
                } else {
                    Log.d("ANITHA", "enabled enableScanner")
                    usbDeviceManager.enableScanner(it, context)
                }
                when (response) {
                    USBConstants.SUCCESS -> {
                        Log.d(
                            TAG,
                            "Scanner enabled successfully"
                        )
                        _status.postValue(DeviceStatus.ENABLED)
//                        usbDeviceStatus[it.usbDevice.productId.toString()] =  Pair(DeviceStatus.ENABLED, false)
//                        _deviceReAttached.postValue(false)
                    }

                    else -> {
                        Log.e(TAG, "Failed to enable the scanner")
                    }
                }
            }
        }
    }

    /**
     * Disable the Connected Scanner.
     */
    fun disable(): Boolean {
        val deferredResult = CompletableDeferred<Boolean>()
        if (!isScanEnable) {
            clearScanData()
        }

        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                //_isLoading.postValue(true)
                val success = when (usbDeviceManager.disableScanner(it)) {
                    USBConstants.SUCCESS -> {

                        Log.d(
                            TAG,
                            "Scanner disable successfully"
                        )
                        if (!isScanEnable) {
                            _status.postValue(DeviceStatus.DISABLE)
                            usbDeviceStatus[it.usbDevice.productId.toString()] =
                                Pair(DeviceStatus.DISABLE, false)
                            //_deviceReAttached.postValue(false)
                        } else {
                            isScanEnable = false
                        }
                        true
                    }

                    else -> {
                        Log.e(TAG, "Failed to disable the scanner")
                        false
                    }


                }
                deferredResult.complete(success)
            }
        } ?: deferredResult.complete(false)

        // Blocking the current thread to wait for the result
        return runBlocking { deferredResult.await() }
    }

    fun setDropdownSelectedDevice(
        currentDevice: UsbDeviceList?,
    ): Boolean {
        usbDeviceStatus[currentDevice?.usbDevice?.productId.toString()]?.let {
            it.let {
                when (it.first) {
                    DeviceStatus.OPENED,
                    DeviceStatus.RELEASED -> {
                        return close()
                    }

                    DeviceStatus.CLAIMED,
                    DeviceStatus.DISABLE -> {
                        if (release()) {
                            return close()
                        }
                    }

                    DeviceStatus.ENABLED -> {
                        if (disable()) {
                            if (release()) {
                                return close()
                            }
                        }
                    }

                    else -> {

                    }
                }
            }
        }

        return false
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("Chethan", "onCleared called")
        usbDeviceManager.unregisterReceiver(context)
    }

    fun appInForeground() {
        selectedDevice.value?.let {
            usbDeviceStatus[selectedDevice.value?.usbDevice?.productId.toString()].let {
                if (it?.first == DeviceStatus.ENABLED) {
                    enabled()
                }
            }
        }
    }

    fun appInBackground() {
        selectedDevice.value?.let {
            usbDeviceStatus[selectedDevice.value?.usbDevice?.productId.toString()].let {
                if (it?.first == DeviceStatus.ENABLED) {
                    isScanEnable = true
                    disable()
                }
            }
        }
    }
}