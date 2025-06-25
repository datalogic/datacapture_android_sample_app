package com.datalogic.aladdin.aladdinusbapp.viewmodel

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.FileUtils
import com.datalogic.aladdin.aladdinusbapp.utils.ResultContants
import com.datalogic.aladdin.aladdinusbapp.utils.USBConstants
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.model.ScaleData
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbScanData
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConfigurationFeature
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DIOCmdValue
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ScaleUnit
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbDioListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbScaleListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbScanListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class HomeViewModel(usbDeviceManager: DatalogicDeviceManager, context: Context) : ViewModel() {
    private var usbDeviceManager: DatalogicDeviceManager

    private val _status = MutableLiveData<DeviceStatus>()
    val status: LiveData<DeviceStatus> = _status

    // Device status message display on UI
    private val _deviceStatus = MutableLiveData<String>()
    val deviceStatus: LiveData<String> = _deviceStatus

    private val _deviceList = MutableLiveData<ArrayList<DatalogicDevice>>(ArrayList())
    val deviceList: LiveData<ArrayList<DatalogicDevice>> = _deviceList

    private val _usbDeviceList = MutableLiveData<ArrayList<UsbDevice>>(ArrayList())
    val usbDeviceList: LiveData<ArrayList<UsbDevice>> = _usbDeviceList

    private val _scanLabel = MutableLiveData("")
    val scanLabel: LiveData<String> = _scanLabel

    private val _scanData = MutableLiveData("")
    val scanData: LiveData<String> = _scanData

    private val _scanRawData = MutableLiveData("")
    val scanRawData: LiveData<String> = _scanRawData

    val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _isLoadingPercent = MutableLiveData(false)
    val isLoadingPercent: LiveData<Boolean> = _isLoadingPercent

    private val _autoDetectChecked = MutableLiveData(true)
    val autoDetectChecked: LiveData<Boolean> = _autoDetectChecked

    val selectedDevice: MutableLiveData<DatalogicDevice?> = MutableLiveData(null)
    val selectedUsbDevice: MutableLiveData<UsbDevice?> = MutableLiveData(null)

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

    // Image capture parameters
    private val _brightness = MutableLiveData("32") // Default 50% (hex "32")
    val brightness: LiveData<String> = _brightness

    private val _contrast = MutableLiveData("32") // Default 50% (hex "32")
    val contrast: LiveData<String> = _contrast

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

    // Scale related properties
    private val _scaleStatus = MutableLiveData<String>("")
    val scaleStatus: LiveData<String> = _scaleStatus

    private val _scaleWeight = MutableLiveData<String>("")
    val scaleWeight: LiveData<String> = _scaleWeight

    private val _scaleUnit = MutableLiveData<ScaleUnit>(ScaleUnit.NONE)
    val scaleUnit: LiveData<ScaleUnit> = _scaleUnit

    /*private val _scaleProtocolStatus = MutableLiveData<Pair<Boolean, String>>(Pair(false, ""))
    val scaleProtocolStatus: LiveData<Pair<Boolean, String>> = _scaleProtocolStatus*/

    private val _isEnableScale = MutableLiveData<Boolean>(false)
    val isEnableScale: LiveData<Boolean> = _isEnableScale

    private val _progressUpgrade = MutableLiveData(0)
    val progressUpgrade: LiveData<Int> = _progressUpgrade

    val _isBulkTransferSupported = MutableLiveData(false)
    val isBulkTransferSupported: LiveData<Boolean> = _isBulkTransferSupported

    //Custom configuration
    private val _customConfiguration =
        MutableLiveData("")
    val customConfiguration: LiveData<String> = _customConfiguration

    //Reset device notify pop-up
    var showResetDeviceDialog by mutableStateOf(false)

    init {
        this.usbDeviceManager = usbDeviceManager
        _status.postValue(DeviceStatus.CLOSED)
        this.context = context
    }

    /**
     * Select a device to work with
     */
    fun setSelectedDevice(device: DatalogicDevice?) {
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

    fun setSelectedUsbDevice(device: UsbDevice?) {
        selectedUsbDevice.value = device
        device?.let {
            _status.postValue(DeviceStatus.CLOSED)
        } ?: run {
            _status.postValue(DeviceStatus.NONE)
        }
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

    fun setAutoDetectChecked(autoDetectChecked: Boolean) {
        _autoDetectChecked.value = autoDetectChecked
        detectDevice()
    }

    /**
     * Check for connected devices
     */
    fun detectDevice() {
        _isLoading.postValue(true)

        if (_autoDetectChecked.value == true) {
            usbDeviceManager.detectDevice(context) { devices ->
                _deviceList.postValue(ArrayList(devices))
                _isLoading.postValue(false)
            }
        } else {
            usbDeviceManager.getAllUsbDevice(context) { devices ->
                _usbDeviceList.postValue(ArrayList(devices))
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Handle device disconnection
     */
    fun handleDeviceDisconnection(device: UsbDevice) {
        clearScanData()
        clearDIOStatus()
        clearScaleData()
        stopScaleHandler()

        // Check if this is our selected device
        selectedDevice.value?.let {
            if (it.usbDevice.productId == device.productId) {
                _status.postValue(DeviceStatus.CLOSED)
                _deviceStatus.postValue("Device disconnected: ${it.displayName}")
                _dioData.postValue("")
            }
            it.handleDeviceDisconnection(device)
        }

        detectDevice()
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

    @JvmOverloads
    fun ByteArray.toHexString(separator: CharSequence = " ",  prefix: CharSequence = "[",  postfix: CharSequence = "]") =
        this.joinToString(separator, prefix, postfix) {
            String.format("0x%02X", it)
        }

    /**
     * Set scanned data from listener
     */
    fun setScannedData(scannedData: UsbScanData) {
        _scanData.postValue(scannedData.barcodeData)
        _scanLabel.postValue(scannedData.barcodeType)
        _scanRawData.postValue(scannedData.rawData.toHexString())
    }

    /**
     * Clear scan data
     */
    fun clearScanData() {
        _scanLabel.postValue("")
        _scanData.postValue("")
        _scanRawData.postValue("")
    }

    /**
     * Handle device reattachment
     */
    fun deviceReAttached(device: UsbDevice) {
        val deviceId = device.productId.toString()
        reattachedDevices.add(deviceId)

        // Update UI
        _deviceStatus.postValue("Device reattached: ${device.productName}")
    }

    /**
     * Open device - perform full open, claim and enable operation
     */
    fun openDevice() {
        if (autoDetectChecked.value == true) {
            selectedDevice.value?.let { device ->
                _isLoading.postValue(true)

                coroutineOpenDevice(device)
            } ?: run {
                // No device selected
                connectDeviceAlert = true
            }
        } else {
            selectedUsbDevice.value?.let { usbDevice ->
                _isLoading.postValue(true)
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
                selectedDevice.value =
                    DatalogicDevice(usbManager, usbDevice, currentDeviceType, currentConnectionType)

                selectedDevice.value?.let { device ->
                    coroutineOpenDevice(device)
                } ?: run {
                    // No device selected
                    connectDeviceAlert = true
                }
            } ?: run {
                // No device selected
                connectDeviceAlert = true
            }
        }
    }

    fun coroutineOpenDevice(device: DatalogicDevice) {
        CoroutineScope(Dispatchers.IO).launch {
            val deviceId = device.usbDevice.productId.toString()

            val result = device.openDevice(context)

            withContext(Dispatchers.Main) {
                when (result) {
                    USBConstants.SUCCESS -> {
                        Log.d(TAG, "Device opened successfully: ${device.displayName}")
                        _deviceStatus.postValue("Device opened")
                        _status.postValue(DeviceStatus.OPENED)

                        // Remove from reattached list since we've handled it
                        reattachedDevices.remove(deviceId)

                        //Setup listener
                        setupCustomListeners(device)
                    }

                    else -> {
                        Log.e(TAG, "Failed to open device: ${device.displayName}")
                        _deviceStatus.postValue("Failed to open device")
                    }
                }

                _isLoading.postValue(false)
            }
        }
    }

    fun setupCustomListeners(device: DatalogicDevice?) {
        if (device != null) {
            //Setup listener
            scanEvent = object : UsbScanListener {
                override fun onScan(scanData: UsbScanData) {
                    setScannedData(scanData)
                }
            }
            device.registerUsbScanListener(scanEvent)

            usbErrorListener = object : UsbDioListener {
                override fun fireDioErrorEvent(
                    errorCode: Int,
                    message: String
                ) {
                    showToast(context, message + errorCode)
                }
            }
            device.registerUsbDioListener(usbErrorListener)

            // Setup scale listener
            val scaleListener = object : UsbScaleListener {
                override fun onScale(scaleData: ScaleData) {
                    // Update UI with scale data on main thread
                    Handler(Looper.getMainLooper()).post {
                        _scaleStatus.postValue(scaleData.status)
                        _scaleWeight.postValue(scaleData.weight)
                        _scaleUnit.postValue(scaleData.unit)
                    }
                }
            }
            device.registerUsbScaleListener(scaleListener)
        }
    }

    /**
     * Close device - perform full disable, release, close operation
     */
    fun closeDevice() {
        selectedDevice.value?.let { device ->
            _isLoading.postValue(true)

            CoroutineScope(Dispatchers.IO).launch {

                val result = device.closeDevice(context)

                withContext(Dispatchers.Main) {
                    when (result) {
                        USBConstants.SUCCESS -> {
                            Log.d(TAG, "Device closed successfully: ${device.displayName}")
                            _deviceStatus.postValue("Device closed")
                            _status.postValue(DeviceStatus.CLOSED)
                            clearScanData()
                            clearConfig()

                            //Clear listeners
                            device.unregisterUsbScanListener(scanEvent)
                            device.unregisterUsbDioListener(usbErrorListener)

                            // Clear scale listener if it was registered
                            if (device.deviceType == DeviceType.FRS) {
                                try {
                                    device.unregisterUsbScaleListener(object : UsbScaleListener {
                                       override fun onScale(scaleData: ScaleData) {}
                                    })
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error unregistering scale listener", e)
                                }
                            }
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
                    if (executeCmd) {
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
            selectedDevice.value?.let {
                if (it.deviceType == DeviceType.HHS && feature == ConfigurationFeature.IMB) {
                    writeConfigData[feature] = if (value) "07" else "00"
                } else {
                    writeConfigData[feature] = if (value) "01" else "00"
                }
            }
        } else {
            writeConfigData.remove(feature)
        }
    }

    /**
     * Dismisses the reset device dialog without performing any device operations.
     * This method is called when the user cancels the reset operation.
     */
    fun dismissResetDialog() {
        showResetDeviceDialog = false
    }

    /**
     * Resets the device and dismisses the dialog.
     * This method is called when the user confirms the reset operation.
     */
    fun resetDevice() {
        selectedDevice.value?.let { device ->
            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                val result = device.resetDevice()

                withContext(Dispatchers.Main) {
                    if (result.equals(USBConstants.SUCCESS)) {
                        _deviceStatus.postValue(ResultContants.DEVICE_RESET_SUCCESS)
                    } else {
                        _deviceStatus.postValue(ResultContants.DEVICE_RESET_FAILED)
                    }
                    _isLoading.postValue(false)
                }
            }
        } ?: run {
            _deviceStatus.postValue("No device selected")
        }
    }
    // Modify the applyConfiguration method to set the dialog state
    fun applyConfiguration() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                resultLiveData.postValue(ResultContants.OPEN_DEVICE_FIRST)
                return
            }

            if (writeConfigData.isEmpty()) {
                resultLiveData.postValue(ResultContants.NO_CONFIGURATION_CHANGES)
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
                            ResultContants.CONFIGURATION_SAVED
                        } else {
                            ResultContants.CONFIGURATION_SAVED_FAILED + "$failure"
                        }
                        withContext(Dispatchers.Main) {
                            resultLiveData.postValue(resultMessage)
                            // Show reset dialog only on successful configuration save
                            if (failure.isEmpty()) {
                                showResetDeviceDialog = true
                            }
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            resultLiveData.postValue(ResultContants.CONFIGURATION_SAVED_FAILED)
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

                    // Clear write data
                    writeConfigData.clear()
                }
            }
        } ?: resultLiveData.postValue(ResultContants.NO_DEVICE_SELECTED)
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
                    val configData = device.readConfig()
                    Log.d("HomeViewModel", "Received config data: $configData")

                    if (configData.isNotEmpty()) {
                        val booleanMap = convertToBoolean(configData)
                        Log.d("HomeViewModel", "Converted to boolean map: $booleanMap")
                        withContext(Dispatchers.Main) {
                            _readConfigData.postValue(HashMap(booleanMap))
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

    /**
     * Function to read custom Configuration of the scanner.
     */
    fun readCustomConfig() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("HomeViewModel", "Reading config data for device: ${device.displayName}")
                    val configData = device.getCustomConfiguration()
                    _customConfiguration.postValue(configData)
                    Log.d(TAG, "Reading custom config data for device: ${device.displayName} value $configData")
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

    /**
     * Function to write custom Configuration of the scanner.
     */
    fun writeCustomConfig(configurationData: String) {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val configData = device.writeCustomConfiguration(configurationData)
                    Log.d(TAG, "Writing custom config data for device: ${device.displayName} value $configData")
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Error writing config: ${e.message}", e)
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

    /**
     * Sets the brightness value for image capture.
     * @param value Brightness value in range 0-100 (percentage)
     */
    fun setBrightness(percentage: Int) {
        if (percentage in 0..100) {
            // Convert percentage (0-100) to hex (00-64)
            val hexValue = percentage.toString(16).padStart(2, '0').uppercase()
            _brightness.postValue(hexValue)
        }
    }

    /**
     * Sets the contrast value for image capture.
     * @param value Contrast value in range 0-100 (percentage)
     */
    fun setContrast(percentage: Int) {
        if (percentage in 0..100) {
            // Convert percentage (0-100) to hex (00-64)
            val hexValue = percentage.toString()
            _contrast.postValue(hexValue)
        }
    }

    /**
     * Get current brightness as an integer percentage (0-100)
     */
    fun getBrightnessPercentage(): Int {
        return try {
            brightness.value?.toInt(16) ?: 50
        } catch (e: Exception) {
            50 // Default to 50% if there's an error
        }
    }

    /**
     * Get current contrast as an integer percentage (0-100)
     */
    fun getContrastPercentage(): Int {
        return try {
            contrast.value?.toInt(16) ?: 50
        } catch (e: Exception) {
            50 // Default to 50% if there's an error
        }
    }

    private var imageResponseCallback: ((Bitmap?) -> Unit)? = null

    fun startCaptureAuto() {
        selectedDevice.value?.let { device ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val currentBrightness = brightness.value ?: "32"  // Default to 50%
                    val currentContrast = contrast.value ?: "32"      // Default to 50%
                    _isLoading.postValue(true)
                    Log.d(
                        TAG,
                        "Image capture with brightness: $currentBrightness, contrast: $currentContrast"
                    )
                    val imageData: ByteArray = device.imageCaptureAuto(
                        currentBrightness.toInt(),
                        currentContrast.toInt()
                    )
                    handleImage(imageData)
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.e(TAG, "Error capturing image: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        resultLiveData.postValue("Image capture failed: ${e.message}")
                    }
                }
            }
        } ?: resultLiveData.postValue("No device selected")
    }

    fun startCaptureOnTrigger() {
        // Implement your on trigger capture logic here.
        println("On Trigger capture triggered")
    }

    private fun handleImage(imageData: ByteArray) {
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

    private fun saveBitmapToGallery(bitmap: Bitmap): Uri? {
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val imageCollection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
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

    /**
     * Handle tab selection logic
     * @param tabIndex The index of the tab to select
     * @return True if the tab should be selected, false if selection should be blocked
     */
    fun handleTabSelection(tabIndex: Int): Boolean {
        // Home tab is always accessible
        if (tabIndex == 0) {
            setSelectedTabIndex(tabIndex)
            // Check scale protocol if available and device is open
            /*if (selectedDevice.value?.deviceType == DeviceType.FRS &&
                status.value == DeviceStatus.OPENED
            ) {
                checkScaleProtocol()
            }*/
            return true
        }

        // For tabs other than Home, we need a device
        if (deviceList.value?.isEmpty() == true && usbDeviceList.value?.isEmpty() == true) {
            connectDeviceAlert = true
            return false
        }

        // For tabs other than Home, device needs to be open
        if (status.value != DeviceStatus.OPENED) {
            openAlert = true
            return false
        }

        // Tab-specific logic
        when (tabIndex) {
            1 -> { // Configuration tab
                if (selectedDevice.value?.connectionType == ConnectionType.USB_OEM) {
                    oemAlert = true
                    return false
                }

                setSelectedTabIndex(tabIndex)
                if (selectedDevice.value?.usbDevice?.productId.toString() == "16386") {
                    magellanConfigAlert = true
                } else {
                    readConfigData()
                }
                return true
            }

            2 -> { // DirectIO tab
                setSelectedTabIndex(tabIndex)
                return true
            }

            3,4,5 -> { // Image capture tab, custom configuration, update firmware
                if (selectedDevice.value?.connectionType == ConnectionType.USB_OEM) {
                    oemAlert = true
                    return false
                }
                setSelectedTabIndex(tabIndex)
                return true
            }

            else -> return false
        }
    }

    /**
     * Check if the selected device has scale protocol enabled
     */
    /*fun checkScaleProtocol() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                _scaleProtocolStatus.postValue(Pair(false, "Device must be opened first"))
                return
            }

            _isLoading.postValue(true)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val protocolStatus = device.checkScaleProtocolEnabled(context)

                    withContext(Dispatchers.Main) {
                        _scaleProtocolStatus.postValue(protocolStatus)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking scale protocol", e)
                    withContext(Dispatchers.Main) {
                        _scaleProtocolStatus.postValue(Pair(false, "Error: ${e.message}"))
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.postValue(false)
                    }
                }
            }
        } ?: _scaleProtocolStatus.postValue(Pair(false, "No device selected"))
    }*/

    /**
     * Enable scale protocol on the selected device
     * Will require device reset to take effect
     */
    /*fun enableScaleProtocol() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                showToast(context, "Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val result = device.enableScaleProtocol(context)

                    withContext(Dispatchers.Main) {
                        if (result == USBConstants.SUCCESS) {
                            showToast(
                                context,
                                "Scale protocol enabled. Device has been reset. Please reconnect."
                            )
                        } else {
                            showToast(context, "Failed to enable scale protocol")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error enabling scale protocol", e)
                    withContext(Dispatchers.Main) {
                        showToast(context, "Error: ${e.message}")
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.postValue(false)
                    }
                }
            }
        } ?: showToast(context, "No device selected")
    }*/

    fun startScaleHandler() {
        selectedDevice.value?.startScale()
        _isEnableScale.postValue(true)
    }

    fun stopScaleHandler() {
        selectedDevice.value?.stopScale()
        _isEnableScale.postValue(false)
    }

    /**
     * Clear scale data
     */
    fun clearScaleData() {
        _scaleStatus.postValue("")
        _scaleWeight.postValue("")
        _scaleUnit.postValue(ScaleUnit.NONE)
    }

    fun saveConfigData(fileName: String) {
        if (!TextUtils.isEmpty(customConfiguration.value.toString()))
        // Save to file if fileName is provided
            if (fileName.isNotEmpty()) {
                FileUtils.saveTextToDownloads(
                    context,
                    fileName,
                    customConfiguration.value.toString()
                )
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.file_name_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        else {
            Toast.makeText(
                context,
                context.getString(R.string.configuration_empty),
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    fun updateCustomConfiguration(newConfig: String) {
        _customConfiguration.value = newConfig
    }

    fun clearConfig(){
        if(selectedDevice.value?.status == DeviceStatus.CLOSED){
            _customConfiguration.value = ""
        }
    }

    fun upgradeFirmware(file: File) {
        _isLoadingPercent.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            selectedDevice.value?.let {
                val firmwareUpdater = it.getFirmwareUpdater(file)
                if (it.deviceType == DeviceType.HHS) {
                    firmwareUpdater.upgrade { progress ->
                        run {
                            _progressUpgrade.postValue(progress)
                        }
                    }
                } else {
                    if (isBulkTransferSupported.value == true) {
                        firmwareUpdater.upgradeByBulkTransfer { progress ->
                            run {
                                _progressUpgrade.postValue(progress)
                            }
                        }
                    } else {
                        firmwareUpdater.upgrade { progress ->
                            run {
                                _progressUpgrade.postValue(progress)
                            }
                        }
                    }
                }
                _isLoadingPercent.postValue(false)
                resetDevice()
            }
        }
    }

    fun getPid(file: File?): String?{
        selectedDevice.value?.let {
            return it.getPid(file)
        }
        return ""
    }

    fun getBulkTransferSupported(file: File?){
        _isLoading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            selectedDevice.value?.let {
                _isBulkTransferSupported.postValue(
                    it.isBulkTransferSupported(file))
                _isLoading.postValue(false)
            }
        }
    }

    fun setBulkTransferSupported(value: Boolean) {
        _isBulkTransferSupported.value = value
    }

    fun isFRS(): Boolean {
        return selectedDevice.value?.deviceType == DeviceType.FRS
    }
}