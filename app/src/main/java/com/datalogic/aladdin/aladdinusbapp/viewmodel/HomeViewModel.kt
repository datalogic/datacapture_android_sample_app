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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeViewModel(usbDeviceManager: USBDeviceManager, context: Context) : ViewModel() {
    private var usbDeviceManager: USBDeviceManager

    private val _status = MutableLiveData<DeviceStatus>()
    val status: LiveData<DeviceStatus> = _status

    private val _deviceStatus = MutableLiveData<String>()
    val deviceStatus: LiveData<String> = _deviceStatus

    private val _deviceList = MutableLiveData<ArrayList<UsbDeviceDescriptor>>(ArrayList())
    val deviceList: LiveData<ArrayList<UsbDeviceDescriptor>> = _deviceList

    private val _scanLabel = MutableLiveData("")
    val scanLabel: LiveData<String> = _scanLabel

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val selectedDevice: MutableLiveData<UsbDeviceDescriptor?> = MutableLiveData(null)

    private val _selectedTabIndex = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    private var TAG: String = HomeViewModel::class.java.simpleName
    private var context: Context

    val usbDeviceStatus: HashMap<String, Pair<DeviceStatus?, Boolean>> = HashMap()

    var claimAlert by mutableStateOf(false)
    var oemAlert by mutableStateOf(false)
    var connectDeviceAlert by mutableStateOf(false)
    var magellanConfigAlert by mutableStateOf(false)

    private var isScanEnable: Boolean = false

    private val _dioStatus = MutableLiveData("")
    val dioStatus: LiveData<String> = _dioStatus

    private val _selectedCommand = MutableLiveData(DIOCmdValue.IDENTIFICATION)
    var selectedCommand: LiveData<DIOCmdValue> = _selectedCommand

    private val _scanData = MutableLiveData("")
    val scanData: LiveData<String> = _scanData

    private val _dioData = MutableLiveData("")
    val dioData: LiveData<String> = _dioData

    private val _readConfigData = MutableLiveData<HashMap<ConfigurationFeature, Boolean>>(hashMapOf())
    val readConfigData : LiveData<HashMap<ConfigurationFeature, Boolean>> = _readConfigData

    var writeConfigData : HashMap<ConfigurationFeature, String> = hashMapOf()

    val resultLiveData = MutableLiveData< String>()
    private var executeCmd = false

    init {
        this.usbDeviceManager = usbDeviceManager
        _status.postValue(DeviceStatus.CLOSED)
        this.context = context
    }

    /**
     * Function to update the selected tab index
     **/
    fun setSelectedTabIndex(index: Int) {
        _selectedTabIndex.value = index
    }

    /**
     * Setting the selected device to variable _selectedDevice
     * @param device is the selected device
     */
    fun setSelectedDevice(device: UsbDeviceDescriptor?) {
        selectedDevice.value = device
        selectedDevice.value?.let { it ->
            val productId = it.usbDevice.productId.toString()
            setDeviceStatus("Attached ${it.usbDevice.productName}")
            usbDeviceStatus[productId].let {
                it?.let {
                    _status.postValue(it.first!!)
                }
            }
        }
        selectedCommand.value?.let { selectedCommand -> updateSelectedDIOCommand(selectedCommand)  }
        _readConfigData.postValue(hashMapOf())
    }


    /**
     * Function to add a new key-value pair to the HashMap if the key does not exist
     * @param map is the Hashmap usbDeviceStatus
     * @param key is the productId as key
     * @param value is the scanner status as value
     */
    private fun addKeyValueIfAbsent(
        map: HashMap<String, Pair<DeviceStatus?, Boolean>>,
        key: String,
        value: DeviceStatus?
    ) {
        if (!map.containsKey(key)) {
            map[key] = Pair(value, false)
        }
    }

    /**
     * Function to check for the connected devices
     */
    fun checkConnectedDevice() {
        _isLoading.postValue(true)

        usbDeviceManager.checkConnectedDevice(context) { usbDevices ->
            val usbDeviceDescriptor = ArrayList<UsbDeviceDescriptor>()

            if (usbDevices.isNotEmpty()) {
                for (device in usbDevices) {
                    usbDeviceDescriptor.add(device)
                    addKeyValueIfAbsent(
                        usbDeviceStatus,
                        device.usbDevice.productId.toString(),
                        DeviceStatus.CLOSED
                    )
                }
            }

            _deviceList.postValue(usbDeviceDescriptor)
            _isLoading.postValue(false)
        }
    }

    /**
     * Function to handle application when the device is detached
     * @param device is the disconnected device
     */
    fun handleDeviceDisconnection(device: UsbDevice) {
        clearScanData()
        selectedDevice.value?.let {
            if(device.productId == it.usbDevice.productId) {
                clearDIOStatus()
                _dioData.postValue("")
                _isLoading.postValue(false)
            }
        }
        usbDeviceManager.handleDeviceDisconnection(device)

        _status.postValue(DeviceStatus.NONE)
        checkConnectedDevice()
    }

    /**
     * Function to set device status message
     * @param status is status message
     */
    fun setDeviceStatus(status: String) {
        _deviceStatus.postValue(status)
    }

    /**
     * Function to set scanned data
     * @param scannedData is scanned data
     */
    fun setScannedData(scannedData: UsbScanData) {
        _scanData.postValue(scannedData.barcodeData)
        _scanLabel.postValue(scannedData.barcodeType)
    }

    /**
     * Function to clear scanned data and label
     */
    fun clearScanData() {
        _scanLabel.postValue("")
        _scanData.postValue("")
    }

    /**
     * Function to handle when device is reattached
     * @param device is the reattached device
     */
    fun deviceReAttached(device: UsbDevice) {

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

    /**
     * Function to set the device status in Hashmap
     * @param productId is the key
     * @param status is value1
     * @param reattached is value2
     */
    fun setStatus(productId: String, status: DeviceStatus, reattached: Boolean) {
        usbDeviceStatus[productId] = Pair(status, reattached)
        _status.postValue(status)
    }


    /**
     * Open the USB connection once permission is granted.
     */
    fun openAndClaimUsbConnection() {
        selectedDevice.value?.let { targetDevice ->
            CoroutineScope(Dispatchers.IO).launch {
                val result = when (usbDeviceManager.openConnection(targetDevice, context)) {
                    USBConstants.SUCCESS -> {
                        Log.d(TAG, "USB Connection opened for device: ${targetDevice.displayName}")
                        usbDeviceStatus[targetDevice.usbDevice.productId.toString()] = Pair(DeviceStatus.OPENED, false)
                        _status.postValue(DeviceStatus.OPENED)
                    }

                    USBConstants.USB_CONNECTION_FAILURE -> {
                        Log.e(TAG, "Failed to open USB connection.")
                    }

                    else -> {
                        Log.e(TAG, "No permission to open USB connection.")
                    }
                }
                // Claim device interface after opening the connection
                if(result == USBConstants.SUCCESS) {
                    claim()
                }
            }
        }
    }

    /**
     * Function to check and return whether the device is reattached or not.
     * @param usbDeviceDescriptor is the device to be checked.
     * @return true if the device is reattached and false if not.
     */
    private fun checkDeviceReattached(usbDeviceDescriptor: UsbDeviceDescriptor): Boolean {
        usbDeviceStatus[usbDeviceDescriptor.usbDevice.productId.toString()]?.let {
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
                val isAttached = checkDeviceReattached(it)
                val response = if (isAttached) {
                    usbDeviceManager.deviceReConnect(it, context)
                } else {
                    usbDeviceManager.claimUsbInterface(it, context)
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
            release()
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

        return runBlocking { deferredResult.await() }
    }

    /**
     * Enable the Connected Scanner.
     */
    fun enabled() {
        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = if (checkDeviceReattached(it)) {
                    usbDeviceManager.deviceReConnect(it, context)
                } else {
                    usbDeviceManager.enableScanner(it, context)
                }
                when (response) {
                    USBConstants.SUCCESS -> {
                        Log.d(TAG, "Scanner enabled successfully")
                        _status.postValue(DeviceStatus.ENABLED)
                        usbDeviceStatus[it.usbDevice.productId.toString()] =  Pair(DeviceStatus.ENABLED, checkDeviceReattached(it))
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
            clearDIOStatus()
        }

        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val success = when (usbDeviceManager.disableScanner(it)) {
                    USBConstants.SUCCESS -> {

                        Log.d(TAG, "Scanner disable successfully")
                        if (!isScanEnable) {
                            _status.postValue(DeviceStatus.DISABLE)
                            usbDeviceStatus[it.usbDevice.productId.toString()] =
                                Pair(DeviceStatus.DISABLE, false)
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

        return runBlocking { deferredResult.await() }
    }

    /**
     * Select device from the Dropdown.
     * @param currentDevice is the device in use.
     * @return false if the current device is closed and close() if it is not closed.
     */
    fun setDropdownSelectedDevice(
        currentDevice: UsbDeviceDescriptor?,
    ): Boolean {
        usbDeviceStatus[currentDevice?.usbDevice?.productId.toString()]?.let {
            it.let {
                _selectedCommand.postValue(DIOCmdValue.IDENTIFICATION)
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
        usbDeviceManager.unregisterReceiver(context)
    }

    /**
     * Function keeps the scanner enabled when application is resumed without stop or pause.
     */
    fun appInForeground() {
        selectedDevice.value?.let {
            usbDeviceStatus[selectedDevice.value?.usbDevice?.productId.toString()].let {
                if (it?.first == DeviceStatus.ENABLED) {
                    enabled()
                }
            }
        }
    }

    /**
     * Function disable the scanner when application is brought to foreground from background.
     */
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

    /**
     * Function updates the DIO Data field with newData.
     * @param newData is the typed data in text field.
     */
    fun updateDIODataField(newData: String) {
        _dioData.postValue(newData)
    }

    /**
     * Function updates the DIO dropdown field and Data field with the command selected from the dropdown.
     * @param command is the selected command.
     */
    fun updateSelectedDIOCommand(command: DIOCmdValue) {
        selectedDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val sb = java.lang.StringBuilder()
                val cmd =
                    if (it.deviceType == USB_OEM) command.oemValue else command.comValue
                if (it.deviceType == USB_OEM) {
                    for (data in cmd) {
                        sb.append(String.format(" 0x%02X", data))
                    }
                    if(command != DIOCmdValue.OTHER) {
                        _dioData.postValue(sb.toString().trim().split( " ").joinToString(","))
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
        selectedDevice.value?.let {
            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                selectedCommand.value?.let { it1 ->
                    executeCmd = false
                    val editText = dioData.value.toString()
                    val validCmd = validationDio()
                    if (!validCmd) {
                        if(isValidHexInput(editText)) {
                            executeCmd = true
                            _selectedCommand.postValue(DIOCmdValue.OTHER)
                            val output = usbDeviceManager.dioCommand(
                                it,
                                DIOCmdValue.OTHER,
                                editText,
                                context,
                            )
                            _dioStatus.postValue(output)
                            _isLoading.postValue(false)
                        } else {
                            executeCmd = true
                            _selectedCommand.postValue(DIOCmdValue.OTHER)
                            _isLoading.postValue(false)
                            _dioStatus.postValue("Not a valid command")
                        }
                    }
                }

            }
        }
    }

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
     * @return true and execute command if data is matching with command, false if not.
     */
    private fun validationDio(): Boolean {
        selectedDevice.value?.let {
            var validation: Boolean
            DIOCmdValue.entries.map { dioCmd ->
                val command = if (it.deviceType == USB_OEM) dioCmd.oemValue else dioCmd.comValue
                if (it.deviceType == USB_COM) {
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
                        val output =  usbDeviceManager.dioCommand(it, dioCmd, dioData.value.toString(), context)
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
                        val output =  usbDeviceManager.dioCommand(it, dioCmd, dioData.value.toString(), context)
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
     * Function to clear DIO status field.
     */
    fun clearDIOStatus() {
        _dioStatus.postValue("")
    }

    /**
     * Function to convert HashMap<ConfigurationFeature, String> to HashMap<ConfigurationFeature, Boolean>.
     * @param map is HashMap<ConfigurationFeature, String>.
     * @return createMap is HashMap<ConfigurationFeature, Boolean>
     */
    private fun convertToBoolean(map: HashMap<ConfigurationFeature, String>) :  HashMap<ConfigurationFeature, Boolean> {
        val createMap : HashMap<ConfigurationFeature, Boolean> = hashMapOf()
        for ((feature, value) in map) {
            if (value == "00") {
                createMap[feature] = false
            } else if(value == "01"){
                createMap[feature] = true
            } else{

            }
        }
        return createMap
    }

    /**
     * Function to update writeConfigData hashmap.
     * @param feature is ConfigurationFeature.
     * @param value is the switch state.
     * @param add is a boolean to add or remove <ConfigurationFeature, Boolean> from the writeConfigData.
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
            CoroutineScope(Dispatchers.IO).launch {
                _isLoading.postValue(true)
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
                writeConfigData= hashMapOf()
                resultLiveData.postValue(failure)
            }
        } ?: resultLiveData.postValue("")
    }

    /**
     * Function to read Configuration of the scanner.
     */
    fun readConfigData() {
        selectedDevice.value?.let { device ->
            CoroutineScope(Dispatchers.IO).launch {
                _isLoading.postValue(true)
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
}