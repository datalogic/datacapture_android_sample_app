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
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.USBConstants
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbDeviceDescriptor
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbScanData
import com.datalogic.aladdin.aladdinusbscannersdk.usbaccess.USBDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.USBConstants.USB_COM
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.USBConstants.USB_OEM
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConfigurationFeature
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DIOCmdValue
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeViewModel(usbDeviceManager: USBDeviceManager, context: Context) : ViewModel() {
    private var usbDeviceManager: USBDeviceManager

    private val _status = MutableLiveData<DeviceStatus>()
    val status: LiveData<DeviceStatus> = _status

    // Device status message display on UI
    private val _deviceStatus = MutableLiveData<String>()
    val deviceStatus: LiveData<String> = _deviceStatus

    private val _deviceList = MutableLiveData<ArrayList<UsbDeviceDescriptor>>(ArrayList())
    val deviceList: LiveData<ArrayList<UsbDeviceDescriptor>> = _deviceList

    private val _scanLabel = MutableLiveData("")
    val scanLabel: LiveData<String> = _scanLabel

    private val _scanData = MutableLiveData("")
    val scanData: LiveData<String> = _scanData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val selectedDevice: MutableLiveData<UsbDeviceDescriptor?> = MutableLiveData(null)

    private val _selectedTabIndex = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    private var TAG: String = HomeViewModel::class.java.simpleName
    private var context: Context

    // Track reattached devices
    private val reattachedDevices = mutableSetOf<String>()

    // UI alert states
    var claimAlert by mutableStateOf(false)
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

    init {
        this.usbDeviceManager = usbDeviceManager
        _status.postValue(DeviceStatus.CLOSED)
        this.context = context
    }

    /**
     * Select a device to work with
     */
    fun setSelectedDevice(device: UsbDeviceDescriptor?) {
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

    /**
     * Check for connected devices
     */
    fun checkConnectedDevice() {
        _isLoading.postValue(true)

        usbDeviceManager.checkConnectedDevice(context) { devices ->
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
        }

        usbDeviceManager.handleDeviceDisconnection(device)
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
                    usbDeviceManager.deviceReConnect(device, context)
                } else {
                    // Standard open operation (combines open, claim, enable)
                    usbDeviceManager.openDevice(device, context)
                }

                withContext(Dispatchers.Main) {
                    when (result) {
                        USBConstants.SUCCESS -> {
                            Log.d(TAG, "Device opened successfully: ${device.displayName}")
                            _deviceStatus.postValue("Device opened")
                            _status.postValue(DeviceStatus.OPENED)

                            // Remove from reattached list since we've handled it
                            reattachedDevices.remove(deviceId)
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
                val result = usbDeviceManager.closeDevice(device.usbDevice)

                withContext(Dispatchers.Main) {
                    when (result) {
                        USBConstants.SUCCESS -> {
                            Log.d(TAG, "Device closed successfully: ${device.displayName}")
                            _deviceStatus.postValue("Device closed")
                            _status.postValue(DeviceStatus.CLOSED)
                            clearScanData()
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
    fun setDropdownSelectedDevice(newDevice: UsbDeviceDescriptor?): Boolean {
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
                val sb = java.lang.StringBuilder()
                val cmd = if (it.deviceType == USB_OEM) command.oemValue else command.comValue

                if (it.deviceType == USB_OEM) {
                    for (data in cmd) {
                        sb.append(String.format(" 0x%02X", data))
                    }
                    if(command != DIOCmdValue.OTHER) {
                        _dioData.postValue(sb.toString().trim().split(" ").joinToString(","))
                    } else {
                        if(executeCmd) {
                            executeCmd = false
                        } else {
                            _dioData.postValue("")
                        }
                    }
                } else {
                    if(command != DIOCmdValue.OTHER) {
                        sb.append(cmd)
                        _dioData.postValue(cmd.joinToString(", ") { it.toInt().toString() })
                    } else {
                        if(executeCmd) {
                            executeCmd = false
                        } else {
                            _dioData.postValue("")
                        }
                    }
                }
            }
        }
        _selectedCommand.postValue(command)
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
                selectedCommand.value?.let { command ->
                    executeCmd = false
                    val editText = dioData.value.toString()
                    val validCmd = validationDio()

                    if (!validCmd) {
                        if(isValidHexInput(editText)) {
                            executeCmd = true
                            _selectedCommand.postValue(DIOCmdValue.OTHER)
                            val output = usbDeviceManager.dioCommand(
                                device,
                                DIOCmdValue.OTHER,
                                editText,
                                context
                            )
                            _dioStatus.postValue(output)
                        } else {
                            executeCmd = true
                            _selectedCommand.postValue(DIOCmdValue.OTHER)
                            _dioStatus.postValue("Not a valid command")
                        }
                    }
                    _isLoading.postValue(false)
                }
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
     * Function to validate hex input for DIO commands
     */
    private fun isValidHexInput(input: String): Boolean {
        return input.split(",").all {
            val value = it.trim()
            if (value.startsWith("0x")) {
                try {
                    Integer.parseInt(value.substring(2), 16)
                    true
                } catch (e: NumberFormatException) {
                    false
                }
            } else {
                try {
                    value.toInt()
                    true
                } catch (e: NumberFormatException) {
                    false
                }
            }
        }
    }

    /**
     * Function to validate the typed DIOData.
     */
    private fun validationDio(): Boolean {
        selectedDevice.value?.let { device ->
            var validation: Boolean
            DIOCmdValue.entries.map { dioCmd ->
                val command = if (device.deviceType == USB_OEM) dioCmd.oemValue else dioCmd.comValue

                if (device.deviceType == USB_COM) {
                    val cmdByteArray: ByteArray
                    try {
                        cmdByteArray = dioData.value?.split(", ")!!.map { it.toInt().toByte() }.toByteArray()
                    } catch (e: NumberFormatException) {
                        _dioStatus.postValue(context.getString(R.string.not_a_valid_command))
                        return false
                    }

                    validation = cmdByteArray.contentEquals(command)
                    if(validation) {
                        _selectedCommand.postValue(dioCmd)
                        val output = usbDeviceManager.dioCommand(device, dioCmd, dioData.value.toString(), context)
                        _dioStatus.postValue(output.replace(",",",\n"))
                        _isLoading.postValue(false)
                        return true
                    }
                } else {
                    val normalizedCmd: String
                    try {
                        normalizedCmd = dioData.value?.split(",")!!.joinToString(",") {
                            val value = it.trim()
                            if (value.startsWith("0x")) value
                            else "0x" + value.toInt().toString(16).uppercase(Locale.ROOT).padStart(2, '0')
                        }
                    } catch (e: NumberFormatException) {
                        _dioStatus.postValue(context.getString(R.string.not_a_valid_command))
                        return false
                    }

                    validation = command.joinToString(",") { String.format("0x%02X", it) } == normalizedCmd
                    if(validation) {
                        _selectedCommand.postValue(dioCmd)
                        val output = usbDeviceManager.dioCommand(device, dioCmd, dioData.value.toString(), context)
                        _dioStatus.postValue(output.replace(",",",\n"))
                        _isLoading.postValue(false)
                        return true
                    }
                }
            }
            return false
        }
        return false
    }

    /**
     * Function to convert HashMap<ConfigurationFeature, String> to HashMap<ConfigurationFeature, Boolean>.
     */
    private fun convertToBoolean(map: HashMap<ConfigurationFeature, String>): HashMap<ConfigurationFeature, Boolean> {
        val createMap: HashMap<ConfigurationFeature, Boolean> = hashMapOf()
        for ((feature, value) in map) {
            when (value) {
                "00" -> createMap[feature] = false
                "01" -> createMap[feature] = true
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

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                val writeResult = withContext(Dispatchers.IO) {
                    usbDeviceManager.writeConfig(device.deviceType, writeConfigData)
                }

                var failure = ""
                if (writeResult.isNotEmpty()) {
                    for ((feature, value) in writeResult) {
                        if (value != ">") failure = failure + feature.featureName + ","
                    }
                } else failure = context.getString(R.string.configuration_save_failure)

                _isLoading.postValue(false)
                readConfigData()
                writeConfigData = hashMapOf()
                resultLiveData.postValue(failure)
            }
        } ?: resultLiveData.postValue("")
    }

    /**
     * Function to read Configuration of the scanner.
     */
    fun readConfigData() {
        selectedDevice.value?.let { device ->
            if (device.status != DeviceStatus.OPENED) {
                _readConfigData.postValue(hashMapOf())
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                val readResult = withContext(Dispatchers.IO) {
                    usbDeviceManager.readConfig(device, context)
                }

                if (readResult.isNotEmpty()) {
                    val createMap = convertToBoolean(readResult)
                    _readConfigData.postValue(createMap)
                    clearScanData()
                }
                _isLoading.postValue(false)
            }
        }
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
}