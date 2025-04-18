package com.datalogic.aladdin.aladdinusbapp.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.datalogic.aladdin.aladdinusbapp.utils.USBConstants
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbScanData
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConfigurationFeature
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DIOCmdValue
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbDioListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbScanListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceType

class HomeViewModel(usbDeviceManager: DatalogicDeviceManager, context: Context) : ViewModel() {
    private var usbDeviceManager: DatalogicDeviceManager

    private val _status = MutableLiveData<DeviceStatus>()
    val status: LiveData<DeviceStatus> = _status

    // Device status message display on UI
    private val _deviceStatus = MutableLiveData<String>()
    val deviceStatus: LiveData<String> = _deviceStatus

    private val _deviceList = MutableLiveData<ArrayList<DatalogicDevice>>(ArrayList())
    val deviceList: LiveData<ArrayList<DatalogicDevice>> = _deviceList

    private val _scanLabel = MutableLiveData("")
    val scanLabel: LiveData<String> = _scanLabel

    private val _scanData = MutableLiveData("")
    val scanData: LiveData<String> = _scanData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val selectedDevice: MutableLiveData<DatalogicDevice?> = MutableLiveData(null)

    private val _selectedTabIndex = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    private var TAG: String = HomeViewModel::class.java.simpleName
    private var context: Context

    // Track reattached devices
    private val reattachedDevices = mutableSetOf<String>()

    // UI alert states
    var openAlert by mutableStateOf(false)
    var oemAlert by mutableStateOf(false)
    var connectDeviceAlert by mutableStateOf(false)
    var magellanConfigAlert by mutableStateOf(false)


    // DIO related state
    private val _dioStatus = MutableLiveData("")
    val dioStatus: LiveData<String> = _dioStatus

    private val _selectedCommand = MutableLiveData(DIOCmdValue.IDENTIFICATION)
    var selectedCommand: LiveData<DIOCmdValue> = _selectedCommand

    private val _dioData = MutableLiveData("")
    val dioData: LiveData<String> = _dioData

    // Configuration related state
    private val _readConfigData = MutableLiveData<HashMap<ConfigurationFeature, Boolean>>(hashMapOf())
    val readConfigData: LiveData<HashMap<ConfigurationFeature, Boolean>> = _readConfigData

    var writeConfigData: HashMap<ConfigurationFeature, String> = hashMapOf()
    val resultLiveData = MutableLiveData<String>()
    // Internal state
    private var executeCmd = false

    //Listener
    private lateinit var scanEvent: UsbScanListener
    private lateinit var usbErrorListener: UsbDioListener

    private var currentDeviceType: DeviceType = DeviceType.HHS
    private var currentConnectionType: ConnectionType = ConnectionType.USB_COM

    init {
        this.usbDeviceManager = usbDeviceManager
        _status.postValue(DeviceStatus.CLOSED)
        this.context = context
    }

    /**
     * Select a device to work with
     */
    fun setSelectedDevice(device: DatalogicDevice?) {
//        // Close any previously selected device
//        selectedDevice.value?.let {
//            if (it.status == DeviceStatus.OPENED) {
//                closeDevice()
//            }
//        }

        selectedDevice.value = device

        // Update UI with selected device info
        device?.let {
            _deviceStatus.postValue("Selected: ${it.displayName}")
            _status.postValue(it.status)
        } ?: run {
            _deviceStatus.postValue("No device selected")
            _status.postValue(DeviceStatus.NONE)
        }

        // Update command dropdown with appropriate command for the device
        selectedCommand.value?.let { updateSelectedDIOCommand(it) }
        _readConfigData.postValue(hashMapOf())
    }

    fun setSelectedDeviceType(deviceType: DeviceType) {
        currentDeviceType = deviceType
    }

    fun getSelectedDeviceType(): DeviceType {
        return currentDeviceType
    }

    fun getSelectedConnectionType(): ConnectionType {
        return currentConnectionType
    }

    fun setSelectedConnectionType(connectionType: ConnectionType) {
        currentConnectionType = connectionType
    }

    /**
     * Check for connected devices
     */
    fun checkConnectedDevice() {
        _isLoading.postValue(true)

        usbDeviceManager.checkConnectedDeviceAsync(context) { devices ->
            _deviceList.postValue(ArrayList(devices))
            _isLoading.postValue(false)
        }
    }

    /**
     * Handle device disconnection
     */
    fun handleDeviceDisconnection(device: UsbDevice) {
        clearScanData()
        clearDIOStatus()

        // Check if this is our selected device
        selectedDevice.value?.let {
            if (it.usbDevice.productId == device.productId) {
                _status.postValue(DeviceStatus.CLOSED)
                _deviceStatus.postValue("Device disconnected: ${it.displayName}")
                _dioData.postValue("")
            }
            it.handleDeviceDisconnection(device)
        }

        checkConnectedDevice()
    }

    /**
     * Set device status message
     */
    fun setDeviceStatus(status: String) {
        _deviceStatus.postValue(status)
    }

    /**
     * Set status from listener callbacks
     */
    fun setStatus(productId: String, status: DeviceStatus) {
        // Only update UI if this status change is for our selected device
        selectedDevice.value?.let {
            if (it.usbDevice.productId.toString() == productId) {
                _status.postValue(status)
            }
        }
    }

    /**
     * Set scanned data from listener
     */
    fun setScannedData(scannedData: UsbScanData) {
        _scanData.postValue(scannedData.barcodeData)
        _scanLabel.postValue(scannedData.barcodeType)
    }

    /**
     * Clear scan data
     */
    fun clearScanData() {
        _scanLabel.postValue("")
        _scanData.postValue("")
    }

    /**
     * Handle device reattachment
     */
    fun deviceReAttached(device: UsbDevice) {
        val deviceId = device.productId.toString()
        reattachedDevices.add(deviceId)

        // Update UI
        _deviceStatus.postValue("Device reattached: ${device.productName}")

        // If this is our selected device, update status
        selectedDevice.value?.let {
            if (it.usbDevice.productId.toString() == deviceId) {
                it.isReattached = true
            }
        }
    }

    /**
     * Open device - perform full open, claim and enable operation
     */
    fun openDevice() {
        selectedDevice.value?.let { device ->
            _isLoading.postValue(true)

            CoroutineScope(Dispatchers.IO).launch {
                val deviceId = device.usbDevice.productId.toString()
                val isReattached = reattachedDevices.contains(deviceId)

                val result = if (isReattached) {
                    // For reattached devices, we need to handle reconnection differently
                    device.deviceReConnect(context)
                } else {
                    // Standard open operation (combines open, claim, enable)
                    device.openDevice(context)
                }

                withContext(Dispatchers.Main) {
                    when (result) {
                        USBConstants.SUCCESS -> {
                            Log.d(TAG, "Device opened successfully: ${device.displayName}")
                            _deviceStatus.postValue("Device opened")
                            _status.postValue(DeviceStatus.OPENED)

                            // Remove from reattached list since we've handled it
                            reattachedDevices.remove(deviceId)

                            //Setup listener
                            scanEvent = object : UsbScanListener {
                                override fun onScan(scanData: UsbScanData) {
                                    setScannedData(scanData)
                                }
                            }
                            device.registerUsbScanListener(scanEvent)

                            usbErrorListener = object : UsbDioListener {
                                override fun fireDioErrorEvent(errorCode: Int, message: String) {
                                    showToast(context, message + errorCode)
                                }
                            }
                            device.registerUsbDioListener(usbErrorListener)
                        }
                        else -> {
                            Log.e(TAG, "Failed to open device: ${device.displayName}")
                            _deviceStatus.postValue("Failed to open device")
                        }
                    }

                    _isLoading.postValue(false)
                }
            }
        } ?: run {
            // No device selected
            connectDeviceAlert = true
        }
    }

    /**
     * Close device - perform full disable, release, close operation
     */
    fun closeDevice() {
        selectedDevice.value?.let { device ->
            _isLoading.postValue(true)

            CoroutineScope(Dispatchers.IO).launch {
                val result = device.closeDevice(device.usbDevice)

                withContext(Dispatchers.Main) {
                    when (result) {
                        USBConstants.SUCCESS -> {
                            Log.d(TAG, "Device closed successfully: ${device.displayName}")
                            _deviceStatus.postValue("Device closed")
                            _status.postValue(DeviceStatus.CLOSED)
                            clearScanData()

                            //Clear listener
                            device.unregisterUsbScanListener(scanEvent)
                            device.unregisterUsbDioListener(usbErrorListener)
                        }
                        else -> {
                            Log.e(TAG, "Failed to close device: ${device.displayName}")
                            _deviceStatus.postValue("Failed to close device")
                        }
                    }

                    _isLoading.postValue(false)
                }
            }
        }
    }

    /**
     * Lifecycle management - handle app going to foreground
     */
    fun appInForeground() {
        selectedDevice.value?.let { device ->
            val deviceId = device.usbDevice.productId.toString()

            // Re-open device if it was reattached while app was in background
            if (device.status == DeviceStatus.CLOSED && reattachedDevices.contains(deviceId)) {
                openDevice()
            }
        }
    }

    /**
     * Lifecycle management - handle app going to background
     */
    fun appInBackground() {
        selectedDevice.value?.let {
            if (it.status == DeviceStatus.OPENED) {
                closeDevice()
            }
        }
    }

    /**
     * Function to update the selected tab index
     */
    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    /**
     * Select device from the Dropdown. Closes the current device if it's open.
     * @param newDevice is the device to select
     * @return true if the selection was processed, false otherwise
     */
    fun setDropdownSelectedDevice(newDevice: DatalogicDevice?): Boolean {
        selectedDevice.value?.let {
            if (it.status == DeviceStatus.OPENED) {
                closeDevice()
            }
        }

        setSelectedDevice(newDevice)
        return true
    }

    /**
     * Function updates the DIO Data field with newData.
     */
    fun updateDIODataField(newData: String) {
        _dioData.postValue(newData)
    }

    /**
     * Function updates the DIO dropdown field and Data field with the command selected from the dropdown.
     */
    fun updateSelectedDIOCommand(command: DIOCmdValue) {
        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (command != DIOCmdValue.OTHER) {
                    // Use the string representation for display
                    val isOem = it.connectionType == ConnectionType.USB_OEM
                    // Use the display string instead of the hex value
                    _dioData.postValue(command.getDisplayString(isOem))
                } else {
                    if(executeCmd) {
                        executeCmd = false
                    } else {
                        _dioData.postValue("")
                    }
                }
                _selectedCommand.postValue(command)
            }
        }
    }

    /**
     * Function to execute the selected command.
     */
    fun executeDIOCommand() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                _dioStatus.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                val commandString = dioData.value.toString()

                if (commandString.isBlank()) {
                    _dioStatus.postValue("Please enter a command")
                    _isLoading.postValue(false)
                    return@launch
                }

                // Get the selected command type
                val selectedCmd = selectedCommand.value ?: DIOCmdValue.OTHER

                // Execute the command
                val output = device.dioCommand(selectedCmd, commandString, context)

                _dioStatus.postValue(output)
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Function to clear DIO status field.
     */
    fun clearDIOStatus() {
        _dioStatus.postValue("")
    }

    /**
     * Function to convert HashMap<ConfigurationFeature, String> to HashMap<ConfigurationFeature, Boolean>.
     */
    private fun convertToBoolean(map: HashMap<ConfigurationFeature, String>): HashMap<ConfigurationFeature, Boolean> {
        val createMap: HashMap<ConfigurationFeature, Boolean> = hashMapOf()
        for ((feature, value) in map) {
            Log.d("HomeViewModel", "Converting feature: $feature, value: $value")
            createMap[feature] = when (value.lowercase()) {
                "00", "0", "false" -> false
                "01", "1", "true" -> true
                else -> value.toIntOrNull()?.let { it > 0 } ?: false
            }
        }
        return createMap
    }

    /**
     * Function to update writeConfigData hashmap.
     */
    fun updateWriteConfigData(feature: ConfigurationFeature, value: Boolean, add: Boolean) {
        if (add) {
            writeConfigData[feature] = if (value) "01" else "00"
        } else {
            writeConfigData.remove(feature)
        }
    }

    /**
     * Function to apply the modified Configuration to the scanner.
     */
    fun applyConfiguration() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            if (writeConfigData.isEmpty()) {
                resultLiveData.postValue("No configuration changes to apply")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("HomeViewModel", "Applying configuration changes: $writeConfigData")

                    val writeResult = device.writeConfig(writeConfigData)
                    Log.d("HomeViewModel", "Write result: $writeResult")

                    var failure = ""
                    if (writeResult.isNotEmpty()) {
                        for ((feature, value) in writeResult) {
                            if (value != ">") failure = failure + feature.featureName + ", "
                        }

                        if (failure.isNotEmpty()) {
                            failure = failure.substring(0, failure.length - 2)
                        }

                        val resultMessage = if (failure.isEmpty()) {
                            "Configuration saved successfully"
                        } else {
                            "Failed to save: $failure"
                        }
                        withContext(Dispatchers.Main) {
                            resultLiveData.postValue(resultMessage)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            resultLiveData.postValue("Configuration save failed")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error in applyConfiguration: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        resultLiveData.postValue("Error: ${e.message}")
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.postValue(false)
                    }

                    // Refresh config data after write
                    delay(500) // Give device time to process changes
                    readConfigData()

                    // Clear write data
                    writeConfigData.clear()
                }
            }
        } ?: resultLiveData.postValue("No device selected")
    }

    /**
     * Function to read Configuration of the scanner.
     */
    fun readConfigData() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("HomeViewModel", "Reading config data for device: ${device.displayName}")
                    val configData = device.readConfig(context)
                    Log.d("HomeViewModel", "Received config data: $configData")

                    if (configData.isNotEmpty()) {
                        val booleanMap = convertToBoolean(configData)
                        Log.d("HomeViewModel", "Converted to boolean map: $booleanMap")
                        withContext(Dispatchers.Main) {
                            _readConfigData.postValue(booleanMap)
                        }
                    } else {
                        Log.e("HomeViewModel", "Received empty config data")
                        withContext(Dispatchers.Main) {
                            resultLiveData.postValue("Failed to read configuration data")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error reading config: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        resultLiveData.postValue("Error: ${e.message}")
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.postValue(false)
                    }
                }
            }
        } ?: resultLiveData.postValue("No device selected")
    }

    public override fun onCleared() {
        super.onCleared()

        // Close any open devices
        selectedDevice.value?.let {
            if (it.status == DeviceStatus.OPENED) {
                closeDevice()
            }
        }

        // Unregister receivers
        usbDeviceManager.unregisterReceiver(context)
    }

    // Function to show Toast on the main thread
    fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }







    private var imageResponseCallback: ((Bitmap?) -> Unit)? = null

    fun startCaptureAuto() {
        selectedDevice.value?.let { device ->
            viewModelScope.launch(Dispatchers.IO) {
                val imageData: ByteArray = device.imageCaptureAuto()
                handleImage(imageData)
            }
        } ?: resultLiveData.postValue("No device selected")
    }

    fun startCaptureOnTrigger() {
        // Implement your on trigger capture logic here.
        println("On Trigger capture triggered")
    }

    private fun handleImage(imageData: ByteArray){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Decode image
                val imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)

                // Save to gallery and get URI
                val imageUri = saveBitmapToGallery(imageBitmap)

                // Notify UI with bitmap and URI on main thread
                withContext(Dispatchers.Main) {
                    imageResponseCallback?.invoke(imageBitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveBitmapToGallery( bitmap: Bitmap): Uri? {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val contentResolver = context.contentResolver
        val imageUri: Uri? = contentResolver.insert(imageCollection, contentValues)
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
        }
        return imageUri
    }

    fun setImageCallback(callback: ((Bitmap?) -> Unit)?) {
        imageResponseCallback = callback
    }
}