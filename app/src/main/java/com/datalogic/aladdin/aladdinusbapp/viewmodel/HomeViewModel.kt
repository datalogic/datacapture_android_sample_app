package com.datalogic.aladdin.aladdinusbapp.viewmodel

import DatalogicBluetoothDevice
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.graphics.scale
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.AboutModel
import com.datalogic.aladdin.aladdinusbapp.utils.CradleState
import com.datalogic.aladdin.aladdinusbapp.utils.FileUtils
import com.datalogic.aladdin.aladdinusbapp.utils.PairingBarcodeType
import com.datalogic.aladdin.aladdinusbapp.utils.PairingStatus
import com.datalogic.aladdin.aladdinusbapp.utils.ResultContants
import com.datalogic.aladdin.aladdinusbapp.utils.USBConstants
import com.datalogic.aladdin.aladdinusbscannersdk.BuildConfig
import com.datalogic.aladdin.aladdinusbscannersdk.feature.upgradefirmware.dfw.UpgradeDFW
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDeviceManager
import com.datalogic.aladdin.aladdinusbscannersdk.model.LabelCodeType
import com.datalogic.aladdin.aladdinusbscannersdk.model.LabelIDControl
import com.datalogic.aladdin.aladdinusbscannersdk.model.ScaleData
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbScanData
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.AladdinConstants.CONFIG_ERR_FAILURE
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.BluetoothPairingStatus
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.BluetoothProfile
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConfigurationFeature
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DIOCmdValue
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ScaleUnit
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.CradleListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbDioListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbScaleListener
import com.datalogic.aladdin.aladdinusbscannersdk.utils.listeners.UsbScanListener
import com.dzungvu.packlog.LogcatHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeViewModel(usbDeviceManager: DatalogicDeviceManager, context: Context, activity: Activity) : ViewModel() {
    var usbDeviceManager: DatalogicDeviceManager
    val tag = HomeViewModel::class.java.simpleName
    private val _status = MutableLiveData<DeviceStatus>()
    val status: LiveData<DeviceStatus> = _status

    // Device status message display on UI
    private val _deviceStatus = MutableLiveData<String>()
    val deviceStatus: LiveData<String> = _deviceStatus

    private val _deviceList = MutableLiveData<ArrayList<DatalogicDevice>>(ArrayList())
    val deviceList: LiveData<ArrayList<DatalogicDevice>> = _deviceList

    private val _usbDeviceList = MutableLiveData<ArrayList<UsbDevice>>(ArrayList())
    val usbDeviceList: LiveData<ArrayList<UsbDevice>> = _usbDeviceList

    private val _allBluetoothDevices = MutableLiveData<ArrayList<DatalogicBluetoothDevice>>(ArrayList())
    val allBluetoothDevices: LiveData<ArrayList<DatalogicBluetoothDevice>> = _allBluetoothDevices

    private fun currentOpenBtDevices(): List<DatalogicBluetoothDevice> =
        _allBluetoothDevices.value?.filter { it.status.value == DeviceStatus.OPENED } ?: emptyList()

    private var bluetoothPollingJob: Job? = null

    private val _scanLabel = MutableLiveData("")
    val scanLabel: LiveData<String> = _scanLabel

    private val _scanData = MutableLiveData("")
    val scanData: LiveData<String> = _scanData

    private val _scanRawData = MutableLiveData("")
    val scanRawData: LiveData<String> = _scanRawData

    private val _customerName = MutableLiveData("")
    val customerName: LiveData<String> = _customerName

    private val _configName = MutableLiveData("")
    val configName: LiveData<String> = _configName

    val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    val _isLoadingPercent = MutableLiveData(false)
    val isLoadingPercent: LiveData<Boolean> = _isLoadingPercent

    private val _autoDetectChecked = MutableLiveData(true)
    val autoDetectChecked: LiveData<Boolean> = _autoDetectChecked

    val selectedDevice: MutableLiveData<DatalogicDevice?> = MutableLiveData(null)
    val selectedUsbDevice: MutableLiveData<UsbDevice?> = MutableLiveData(null)

    val selectedBluetoothDevice: MutableLiveData<DatalogicBluetoothDevice?> = MutableLiveData(null)
    val selectedScannerBluetoothDevice: MutableLiveData<DatalogicBluetoothDevice?> =
        MutableLiveData(null)

    val selectedBluetoothProfile: MutableLiveData<PairingBarcodeType?> = MutableLiveData(null)
    val previousBluetoothProfile: MutableLiveData<PairingBarcodeType?> = MutableLiveData(null)
    val currentPairingStatus: MutableLiveData<PairingStatus?> = MutableLiveData(null)
    val currentBleDeviceName: MutableLiveData<String> = MutableLiveData(null)

    private val _selectedTabIndex = MutableLiveData(0)
    val selectedTabIndex: LiveData<Int> = _selectedTabIndex

    private var context: Context

    private var activity: Activity? = null

    // Track reattached devices
    private val reattachedDevices = mutableSetOf<String>()

    // Log toggle state
    private val _isLoggingEnabled = MutableLiveData(false)
    val isLoggingEnabled: LiveData<Boolean> = _isLoggingEnabled

    private val _isDebugEnabled = MutableLiveData(false)
    val isDebugEnabled: LiveData<Boolean> = _isDebugEnabled
    val aboutApp: MutableLiveData<AboutModel?> = MutableLiveData(null)

    // Connect type toggle state
    private val _isBluetoothEnabled = MutableLiveData(false)
    val isBluetoothEnabled: LiveData<Boolean> = _isBluetoothEnabled
    private val _isCheckDockingEnabled = MutableLiveData(false)
    val isCheckDockingEnabled: LiveData<Boolean> = _isCheckDockingEnabled
    private val _deviceCradleStates = MutableStateFlow<List<CradleState>>(emptyList())
    val deviceCradleStates = _deviceCradleStates.asStateFlow()
    private val activeCradleListeners = mutableMapOf<DatalogicDevice, CradleListener>()

    // Label parsing settings
    private val _selectedLabelCodeType = MutableLiveData(LabelCodeType.NONE)
    val selectedLabelCodeType: LiveData<LabelCodeType> = _selectedLabelCodeType

    private val _selectedLabelIDControl = MutableLiveData(LabelIDControl.DISABLE)
    val selectedLabelIDControl: LiveData<LabelIDControl> = _selectedLabelIDControl

    // LogcatHelper instance - direct usage without Application dependency
    private val logcatHelper: LogcatHelper by lazy {
        LogcatHelper.LogcatBuilder()
            .setMaxFileSize(2 * 1024 * 1024) // 2MB
            .setMaxFolderSize(10 * 1024 * 1024) // 10MB
            .build(context)
    }

    // UI alert states
    var openAlert by mutableStateOf(false)
    var oemAlert by mutableStateOf(false)
    var bluetoothAlert by mutableStateOf(false)
    var connectDeviceAlert by mutableStateOf(false)
    var magellanConfigAlert by mutableStateOf(false)
    var noDeviceSupportAlert by mutableStateOf(false)
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
    private val _readConfigData =
        MutableLiveData<HashMap<ConfigurationFeature, Boolean>>(hashMapOf())
    val readConfigData: LiveData<HashMap<ConfigurationFeature, Boolean>> = _readConfigData

    var writeConfigData: HashMap<ConfigurationFeature, String> = hashMapOf()
    val resultLiveData = MutableLiveData<String>()

    // Internal state
    private var executeCmd = false

    //Listener
    private var scanEvent: UsbScanListener? = null
    private var usbErrorListener: UsbDioListener? = null
    private var bluetoothErrorListener: UsbDioListener? = null

    private var bluetoothScanEvent: UsbScanListener? = null

    private var scaleListener: UsbScaleListener? = null

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

    private val _isScaleAvailable = MutableLiveData<Boolean>(false)
    val isScaleAvailable: LiveData<Boolean> = _isScaleAvailable

    private val _progressUpgrade = MutableLiveData(0)
    val progressUpgrade: LiveData<Int> = _progressUpgrade

    val _isBulkTransferSupported = MutableLiveData(false)
    val isBulkTransferSupported: LiveData<Boolean> = _isBulkTransferSupported

    private var _isCheckPid = MutableStateFlow(false)
    val isCheckPid: StateFlow<Boolean> = _isCheckPid.asStateFlow()

    //Custom configuration
    private val _customConfiguration =
        MutableLiveData("")
    val customConfiguration: LiveData<String> = _customConfiguration

    //Reset device notify pop-up
    var showResetDeviceDialog by mutableStateOf(false)

    private val _qrBitmap = MutableLiveData<Bitmap>()
    val qrBitmap: LiveData<Bitmap> get() = _qrBitmap

    private val _msgConfigError = MutableLiveData("")
    val msgConfigError: LiveData<String> = _msgConfigError

    private val mainHandler = Handler(Looper.getMainLooper())
    private val bufferBluetoothData = ArrayDeque<ByteArray>()

    data class ScanUi(
        val data: String = "",
        val label: String = "",
        val raw: String = "",
        val seq: Long = 0L, // unique per emission
    )

    class MultiScannerRegistry {
        private val map = LinkedHashMap<String, MutableStateFlow<ScanUi>>()

        fun flowFor(deviceId: String): StateFlow<ScanUi> =
            map.getOrPut(deviceId) { MutableStateFlow(ScanUi()) }.asStateFlow()

        fun emit(deviceId: String, next: ScanUi) {
            val flow = map.getOrPut(deviceId) { MutableStateFlow(ScanUi()) }
            flow.update { next }
        }

        fun clear(deviceId: String) {
            map[deviceId]?.update { ScanUi() }
        }
    }

    private val perDeviceScan = MultiScannerRegistry()

    fun scanFlowFor(deviceId: String): StateFlow<ScanUi> = perDeviceScan.flowFor(deviceId)

    fun perDeviceClear(deviceId: String) { perDeviceScan.clear(deviceId) }

    fun emitScanFrom(deviceId: String, data: UsbScanData) {
        perDeviceScan.emit(
            deviceId,
            ScanUi(
                data = data.barcodeData,
                label = data.barcodeType,
                raw = data.rawData.toHexString(),
                seq = SystemClock.uptimeMillis()
            )
        )
    }

    // -------- Per-device Scale stream --------
    data class ScaleUi(
        val status: String = "",
        val weight: String = "",
        val unit: ScaleUnit = ScaleUnit.NONE,
        val seq: Long = 0L, // bump to force recomposition when values repeat
    )


    class MultiScaleRegistry {
        private val flows = LinkedHashMap<String, MutableStateFlow<ScaleUi>>()
        private val enabled = LinkedHashMap<String, MutableStateFlow<Boolean>>() // <- observable

        fun flowFor(deviceId: String): StateFlow<ScaleUi> =
            flows.getOrPut(deviceId) { MutableStateFlow(ScaleUi()) }

        fun enabledFlowFor(deviceId: String): StateFlow<Boolean> =
            enabled.getOrPut(deviceId) { MutableStateFlow(false) }

        fun emit(deviceId: String, next: ScaleUi) {
            flows.getOrPut(deviceId) { MutableStateFlow(ScaleUi()) }.value = next
        }

        fun setEnabled(deviceId: String, isEnabled: Boolean) {
            enabled.getOrPut(deviceId) { MutableStateFlow(false) }.value = isEnabled
        }

        fun clear(deviceId: String) {
            flows[deviceId]?.value = ScaleUi()
            enabled[deviceId]?.value = false
        }
    }

    fun scaleEnabledFlowFor(deviceId: String): StateFlow<Boolean> = perDeviceScale.enabledFlowFor(deviceId)

    private val perDeviceScale = MultiScaleRegistry()

    fun scaleFlowFor(deviceId: String): StateFlow<ScaleUi> = perDeviceScale.flowFor(deviceId)

    fun emitScaleFrom(deviceId: String, sd: ScaleData) {
        perDeviceScale.emit(
            deviceId,
            ScaleUi(
                status = sd.status,
                weight = sd.weight,
                unit = sd.unit,
                seq = SystemClock.uptimeMillis()
            )
        )
        // (Optional) Keep legacy globals in sync for old screens:
        _scaleStatus.postValue(sd.status)
        _scaleWeight.postValue(sd.weight)
        _scaleUnit.postValue(sd.unit)
    }

    fun perDeviceScaleClear(deviceId: String) { perDeviceScale.clear(deviceId) }

    // Convenience overloads
    fun perDeviceScaleClear(device: DatalogicDevice?) {
        device?.let { perDeviceScale.clear(it.usbDevice.deviceId.toString()) }
    }

    init {
        this.usbDeviceManager = usbDeviceManager
        _status.postValue(DeviceStatus.CLOSED)
        this.context = context
        this.activity = activity

        currentPairingStatus.value = PairingStatus.Idle
        selectedBluetoothProfile.value = PairingBarcodeType.SPP
        previousBluetoothProfile.value = PairingBarcodeType.UNLINK
        _customerName.value = "Datalogic"
        _configName.value = ""
        logcatHelper.start()
    }

    /**
     * Select a device to work with
     */
    fun setSelectedDevice(device: DatalogicDevice?) {
        selectedDevice.value = device

        // Update UI with selected device info
        device?.let {
            Log.d(tag,"[setSelectedDevice] Selected: ${it.displayName}")
            _deviceStatus.postValue("Selected: ${it.displayName}")
            _status.postValue(it.status.value)
        } ?: run {
            _deviceStatus.postValue("No device selected")
            _status.postValue(DeviceStatus.NONE)
        }

        // Update command dropdown with appropriate command for the device
        selectedCommand.value?.let { updateSelectedDIOCommand(it) }
        _readConfigData.postValue(hashMapOf())
        selectedBluetoothDevice.value = null
    }

    fun setSelectedUsbDevice(device: UsbDevice?) {
        selectedUsbDevice.value = device
        device?.let {
            _status.postValue(DeviceStatus.CLOSED)
        } ?: run {
            _status.postValue(DeviceStatus.NONE)
        }
        selectedBluetoothDevice.value = null
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
                handleSelectDevice()
            }
            usbDeviceManager.getAllBluetoothDevice(activity!!) { devices ->
                _allBluetoothDevices.postValue(ArrayList(devices))
                _isLoading.postValue(false)
                handleSelectDevice()
            }
        }
        usbDeviceManager.getAllUsbDevice(context) { devices ->
            _usbDeviceList.postValue(ArrayList(devices))
            _isLoading.postValue(false)
            handleSelectDevice()

            if (_autoDetectChecked.value == false) {
                val connectedIds = devices.map { it.deviceId }.toSet()
                val list = _deviceList.value ?: arrayListOf()
                var mutated = false
                list.forEach { dev ->
                    if (dev.usbDevice.deviceId !in connectedIds && dev.status.value == DeviceStatus.OPENED) {
                        dev.status.value = DeviceStatus.CLOSED
                        mutated = true
                    }
                }
                if (mutated) _deviceList.postValue(ArrayList(list))
            }
        }
    }

    private fun handleSelectDevice() {
        val openUsb = _deviceList.value?.filter { it.status.value == DeviceStatus.OPENED }.orEmpty()
        val openBt  = _allBluetoothDevices.value?.filter { it.status.value == DeviceStatus.OPENED }.orEmpty()

        if (selectedDevice.value != null || selectedBluetoothDevice.value != null) return

        when {
            openBt.isNotEmpty()  -> setSelectedBluetoothDevice(openBt.first())
            else -> {
                setSelectedBluetoothDevice(null)
            }
        }
    }

    /**
     * Handle device disconnection
     */
    fun handleDeviceDisconnection(disconnectDevice: UsbDevice) {
        perDeviceClear(disconnectDevice.deviceId.toString())
        clearDIOStatus()
        clearScaleData(disconnectDevice.deviceId.toString())
        stopScaleHandler(disconnectDevice.deviceId.toString())
        //Disable scale section
        _isScaleAvailable.postValue(false)
        if (!deviceList.value.isNullOrEmpty()) {
            for (device in deviceList.value!!) {
                if (device.usbDevice.deviceName == disconnectDevice.deviceName) {
                    if (selectedDevice.value?.usbDevice?.deviceName == disconnectDevice.deviceName) {
                        _status.postValue(DeviceStatus.CLOSED)
                        _deviceStatus.postValue("Device disconnected: ${device.displayName}")
                        _dioData.postValue("")
                    }
                    device.handleDeviceDisconnection(disconnectDevice)
                    break
                }
            }
        }

        detectDevice()
        Log.d("HomeViewModel", "[handleDeviceDisconnection] removeDeviceCradleState")
        removeDeviceCradleState(disconnectDevice)
    }

    fun handleBluetoothDeviceDisconnection(device: BluetoothDevice) {
        perDeviceClear(device.address)
        clearDIOStatus()
        selectedScannerBluetoothDevice.value?.let {
            if (it.bluetoothDevice.address == device.address) {
                _status.postValue(DeviceStatus.CLOSED)
                _deviceStatus.postValue("Device disconnected: ${selectedScannerBluetoothDevice.value?.name}")
                _dioData.postValue("")
            }
        }
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
        if (isBluetoothEnabled.value == true) {
            _status.postValue(status)
        } else {
            selectedDevice.value?.let {
                if (it.usbDevice.productId.toString() == productId) {
                    _status.postValue(status)
                }
            }
        }
    }

    @JvmOverloads
    fun ByteArray.toHexString(
        separator: CharSequence = " ",
        prefix: CharSequence = "[",
        postfix: CharSequence = "]"
    ) =
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
    // Convenience: clear by USB device object
    fun perDeviceClear(device: DatalogicDevice?) {
        device?.let { perDeviceScan.clear(it.usbDevice.deviceId.toString()) }
    }

    // Convenience: clear by Bluetooth device object
    fun perDeviceClear(device: DatalogicBluetoothDevice?) {
        device?.let { perDeviceScan.clear(it.bluetoothDevice.address) }
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
    fun openUsbDevice(datalogicDevice: DatalogicDevice?) {
        Log.d(tag, "[openDevice] Begin")
        if (autoDetectChecked.value == true) {
            datalogicDevice?.let { device ->
                _isLoading.postValue(true)
                coroutineOpenDevice(datalogicDevice)
            } ?: run {
                // No device selected
                connectDeviceAlert = true
            }
        } else {
            selectedUsbDevice.value?.let { usbDevice ->
                _isLoading.postValue(true)
                val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
//                selectedDevice.value =
//                    DatalogicDevice(usbManager, usbDevice, currentDeviceType, currentConnectionType)

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
            // Open the device
            val result = device.openDevice(context)

            withContext(Dispatchers.Main) {
                when (result) {
                    USBConstants.SUCCESS -> {
                        onOpenDeviceSuccessResultAction(deviceId)
                        //Setup listener
                        setupCustomListeners(device)
                        // Initialize label settings from device
                        initializeLabelSettingsFromDevice()
                        device.status.value = DeviceStatus.OPENED
                        updateDeviceStatusInList(device, DeviceStatus.OPENED)
                    }

                    else -> {
                        Log.e(tag, "Failed to open device: ${device.displayName}")
                        _deviceStatus.postValue("Failed to open device")
                    }
                }

                _isLoading.postValue(false)
            }
        }
    }

    fun updateDeviceStatusInList(target: DatalogicDevice, newStatus: DeviceStatus) {
        val list = _deviceList.value ?: return

        // Prefer a stable, session-level id. Fall back to VID:PID if needed.
        val idx = list.indexOfFirst { dev ->
            dev.usbDevice.deviceId == target.usbDevice.deviceId &&
                    dev.usbDevice.deviceName == target.usbDevice.deviceName
        }

        if (idx >= 0) {
            val device = list[idx]
            // Update the device’s own LiveData so rows bound to it recompose
            device.status.value = newStatus

            // Post a new list instance so collectors of deviceList see the change
            _deviceList.postValue(ArrayList(list))
        }
    }

    fun isOpenDevice(): Boolean {
        _deviceList.value?.let {
            _deviceList.value?.forEach { item ->
                if (item.status.value == DeviceStatus.OPENED) {
                    return true
                }
            }
        }
        _allBluetoothDevices.value?.let {
            _allBluetoothDevices.value?.forEach { item ->
                if (item.status.value == DeviceStatus.OPENED) {
                    return true
                }
            }
        }
        return false
    }

    fun onOpenDeviceSuccessResultAction(deviceId: String) {
        val device: DatalogicDevice = selectedDevice.value ?: run {
            Log.e(tag, "No device selected for opening")
            _deviceStatus.postValue("No device selected")
            return
        }

        Log.d(tag, "Device opened successfully: ${device.displayName}")
        _deviceStatus.postValue("Device opened")
        _status.postValue(DeviceStatus.OPENED)
        if (device.isScaleAvailable()) {
            _isScaleAvailable.postValue(true)
        }
        // Remove from reattached list since we've handled it
        reattachedDevices.remove(deviceId)
    }

    fun setupCustomListeners(device: DatalogicDevice?) {
        if (device != null) {
            //Setup listener
            scanEvent = object : UsbScanListener {
                override fun onScan(scanData: UsbScanData) {
                    emitScanFrom(device.usbDevice.deviceId.toString(), scanData)
                }
            }
            scanEvent?.let {
                device.registerUsbScanListener(it)
            }

            usbErrorListener = object : UsbDioListener {
                override fun fireDioErrorEvent(
                    errorCode: Int,
                    message: String
                ) {
                    if (errorCode == CONFIG_ERR_FAILURE) {
                        setMsgConfigError(message + errorCode)
                    } else {
                        showToast(context, message + errorCode)
                    }
                }
            }
            usbErrorListener?.let {
                device.registerUsbDioListener(it)
            }
            // Setup scale listener
            scaleListener = object : UsbScaleListener {
                override fun onScale(scaleData: ScaleData) {
                    // Update UI with scale data on main thread
                    val id = device.usbDevice.deviceId.toString()
                    perDeviceScale.emit(
                        id,
                        ScaleUi(
                            status = scaleData.status,
                            weight = scaleData.weight,
                            unit = scaleData.unit,
                            seq = SystemClock.uptimeMillis()
                        )
                    )
                }
            }
            scaleListener?.let {
                device.registerUsbScaleListener(it)
            }
        }
    }

    /**
     * Close device - perform full disable, release, close operation
     */
    fun closeUsbDevice(device: DatalogicDevice?) {
        device?.let { device ->
            _isLoading.postValue(true)

            CoroutineScope(Dispatchers.IO).launch {

                val result = device.closeDevice(context)

                withContext(Dispatchers.Main) {
                    when (result) {
                        USBConstants.SUCCESS -> {
                            Log.d(tag, "Device closed successfully: ${device.displayName}")
                            _deviceStatus.postValue("Device closed")
                            _status.postValue(DeviceStatus.CLOSED)
                            device.status.value = DeviceStatus.CLOSED
                            perDeviceClear(device.usbDevice.deviceId.toString())
                            perDeviceScaleClear(device.usbDevice.deviceId.toString())
                            clearScaleData(device.usbDevice.deviceId.toString())
                            clearConfig()

                            _isScaleAvailable.postValue(false)

                            //Clear listeners
                            scanEvent?.let {
                                device.unregisterUsbScanListener(it)
                            }
                            usbErrorListener?.let {
                                device.unregisterUsbDioListener(it)
                            }
                            scaleListener?.let {
                                device.unregisterUsbScaleListener(it)
                            }
                            // Clear scale listener if it was registered
                            if (device.deviceType == DeviceType.FRS) {
                                try {
                                    device.unregisterUsbScaleListener(object : UsbScaleListener {
                                        override fun onScale(scaleData: ScaleData) {}
                                    })
                                } catch (e: Exception) {
                                    Log.e(tag, "Error unregistering scale listener", e)
                                }
                            }
                            if (selectedDevice.value?.usbDevice?.deviceName == device.usbDevice.deviceName) {
                                setSelectedDevice(null)
                            }
                            updateDeviceStatusInList(device, DeviceStatus.CLOSED)
                        }

                        else -> {
                            Log.e(tag, "Failed to close device: ${device.displayName}")
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
    fun appInForeground(activity: Activity) {
        selectedDevice.value?.let { device ->
            val deviceId = device.usbDevice.productId.toString()

            // Re-open device if it was reattached while app was in background
            if (device.status.value == DeviceStatus.CLOSED && reattachedDevices.contains(deviceId)) {
                //openDevice(activity)
            }
        }
    }

    /**
     * Lifecycle management - handle app going to background
     */
    fun appInBackground() {
        selectedDevice.value?.let {
            if (it.status.value == DeviceStatus.OPENED) {
                //closeUsbDevice()
            }
        }
        selectedScannerBluetoothDevice.value?.let {
            if (it.status.value == DeviceStatus.OPENED) {
                //closeBluetoothDevice()
            }
        }
    }

    /**
     * Function to update the selected tab index
     */
    fun setSelectedTabIndex(index: Int) {
        if(!UpgradeDFW.isUpgradeDFW) {
            _selectedTabIndex.value = index
        }
    }

    /**
     * Select device from the Dropdown. Closes the current device if it's open.
     * @param newDevice is the device to select
     * @return true if the selection was processed, false otherwise
     */
    fun setDropdownSelectedDevice(newDevice: DatalogicDevice?): Boolean {
        selectedDevice.value?.let { current ->
            if (current.status.value == DeviceStatus.OPENED && current != newDevice) {
                // FIX: close the CURRENT device, not the new one
                closeUsbDevice(current)
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

    fun updateCustomerName(name: String) {
        _customerName.postValue(name.trim())
    }

    fun updateCurrentConfigName(name: String) {
        _configName.postValue(name)
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
        selectedBluetoothDevice.value?.let {
            CoroutineScope(Dispatchers.IO).launch {
                if (command != DIOCmdValue.OTHER) {
                    // Use the string representation for display
                    val isOem = false
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
        Log.d(tag, "[executeDIOCommand] isBluetoothEnabled: ${isBluetoothEnabled.value}")
        selectedDevice.value?.let { device ->
            if (!isOpenDevice()) {
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
        selectedBluetoothDevice.value?.let { device ->
            if (!isOpenDevice()) {
                Log.d(tag, "[executeDIOCommand] device.status: ${device.status}")

                _dioStatus.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                val commandString = dioData.value.toString()

                if (commandString.isBlank()) {
                    Log.d(tag, "[executeDIOCommand] Please enter a command")

                    _dioStatus.postValue("Please enter a command")
                    _isLoading.postValue(false)
                    return@launch
                }

                // Get the selected command type
                val selectedCmd = selectedCommand.value ?: DIOCmdValue.OTHER

                Log.d(tag, "[executeDIOCommand] selectedCommand: $selectedCommand")
                // Execute the command
                val output = device.dioCommand(selectedCmd, commandString, context)

                _dioStatus.postValue(output)
                Log.d(tag, "[executeDIOCommand] output: $output")

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
    fun resetDeviceExitedServiceMode() {
        selectedDevice.value?.let { device ->
            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                var result = device.enterServiceMode()
                result = device.resetDevice()
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
            if (device.status.value != DeviceStatus.OPENED) {
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
                    Log.d(tag, "Applying configuration changes: $writeConfigData")

                    val writeResult = device.writeConfig(writeConfigData)
                    Log.d(tag, "Write result: $writeResult")

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
                    Log.e(tag, "Error in applyConfiguration: ${e.message}", e)
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
            if (!isOpenDevice()) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d(tag, "Reading config data for device: ${device.displayName}")
                    val configData = device.readConfig()
                    Log.d(tag, "Received config data: $configData")

                    if (configData.isNotEmpty()) {
                        val booleanMap = convertToBoolean(configData)
                        Log.d(tag, "Converted to boolean map: $booleanMap")
                        withContext(Dispatchers.Main) {
                            _readConfigData.postValue(HashMap(booleanMap))
                        }
                    } else {
                        Log.e(tag, "Received empty config data")
                        withContext(Dispatchers.Main) {
                            resultLiveData.postValue("Failed to read configuration data")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error reading config: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        resultLiveData.postValue("Error: ${e.message}")
                    }
                } finally {
                    withContext(Dispatchers.Main) {
                        _isLoading.postValue(false)
                    }
                }
            }
        }
    }

    /**
     * Function to read custom Configuration of the scanner.
     */
    fun getDeviceConfigName() {
        selectedDevice.value?.let { device ->
            val name = device.getConfigName(context)
            Log.d(tag,"[getDeviceConfigName] ${device.displayName} config name $name")
            updateCurrentConfigName(name)
        }
    }

    fun readCustomConfig() {
        selectedDevice.value?.let { device ->
            if (!isOpenDevice()) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (customerName.value != null && configName.value != null) {
                        val configData = device.getCustomConfiguration(customerName.value!!, configName.value!!)
                        _customConfiguration.postValue(configData)
                        Log.d(tag,"Reading custom config data for device: ${device.displayName} value $configData")
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error reading config: ${e.message}", e)
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
            if (!isOpenDevice()) {
                resultLiveData.postValue("Device must be opened first")
                return
            }

            _isLoading.postValue(true)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val configResult = device.writeCustomConfiguration(configurationData)
                    Log.d(tag, "Writing custom config data for device: ${device.displayName}")

                    withContext(Dispatchers.Main) {
                        if (configResult.isSuccess) {
                            resultLiveData.postValue("Configuration applied successfully")
                            configurationCallback?.onConfigurationResult(
                                true,
                                "SUCCESSFULLY",
                                "Configuration applied successfully"
                            )
                        } else {
                            val errorMessage = buildString {
                                append("Configuration failed with ${configResult.errorCommands.size} error(s):\n\n")
                                configResult.errorCommands.forEachIndexed { index, error ->
                                    append("Line ${error.rowNumber}: ${error.rowData}")
                                    if (error.errorMessage.isNotEmpty()) {
                                        append("\n- ${error.errorMessage}")
                                    }
                                    if (index < configResult.errorCommands.size - 1) {
                                        append("\n\n")
                                    }
                                }
                            }
                            resultLiveData.postValue(errorMessage)
                            configurationCallback?.onConfigurationResult(
                                false,
                                "ERROR",
                                errorMessage
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error writing config: ${e.message}", e)
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

        // Stop logging when ViewModel is cleared
        logcatHelper.stop()

        // Close any open devices
        selectedDevice.value?.let {
            if (it.status.value == DeviceStatus.OPENED) {
                closeUsbDevice(it)
            }
        }
        selectedScannerBluetoothDevice.value?.let {
            if (it.status.value == DeviceStatus.OPENED) {
                closeBluetoothDevice(it)
            }
        }

        // Unregister receivers
        usbDeviceManager.unregisterReceiver(context)
        closeAllDevices()
    }

    // Function to show Toast on the main thread
    fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    // Callback interface for configuration results
    interface ConfigurationResultCallback {
        fun onConfigurationResult(isSuccess: Boolean, title: String, message: String)
    }

    private var configurationCallback: ConfigurationResultCallback? = null

    fun setConfigurationResultCallback(callback: ConfigurationResultCallback?) {
        configurationCallback = callback
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
                        tag,
                        "Image capture with brightness: $currentBrightness, contrast: $currentContrast"
                    )
                    val imageData: ByteArray = device.imageCaptureAuto(
                        currentBrightness.toInt(),
                        currentContrast.toInt()
                    )
                    handleImage(imageData)
                    _isLoading.postValue(false)
                } catch (e: Exception) {
                    Log.e(tag, "Error capturing image: ${e.message}", e)
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

            if (_selectedTabIndex.value == 6) {
                usbDeviceManager.stopScanBluetoothDevices(context)
            }
            // Check scale protocol if available and device is open
            /*if (selectedDevice.value?.deviceType == DeviceType.FRS &&
                status.value == DeviceStatus.OPENED
            ) {
                checkScaleProtocol()
            }*/
            return true
        }

        if (tabIndex == 6) {
            setSelectedTabIndex(tabIndex)
            setPairingStatus(PairingStatus.Idle)
            return true
        }

        if (tabIndex == 7) {
            setSelectedTabIndex(tabIndex)
            return true
        }

        // For tabs other than Home, we need a device
        if (deviceList.value?.isEmpty() == true && usbDeviceList.value?.isEmpty() == true && allBluetoothDevices.value?.isEmpty() == true) {
            connectDeviceAlert = true
            return false
        }

        // For tabs other than Home, device needs to be open
        if (!isOpenDevice()) {
            Log.d(tag, "[handleTabSelection] status: ${status.value}")
            openAlert = true
            return false
        }
        val listUsbOpened = deviceList.value?.filter { it.status.value == DeviceStatus.OPENED}
        val listDeviceSupport = listUsbOpened?.filter { //Device USB-COM or COM-SC with Service port
            it.usbDevice.productId.toString() != "16386" && it.connectionType != ConnectionType.USB_OEM
        }
        val listUsbOem = listUsbOpened?.filter { it.connectionType == ConnectionType.USB_OEM }
        val listMagellanConfig = listUsbOpened?.filter { it.usbDevice.productId.toString() == "16386" }

        val deviceSupportExist = listDeviceSupport?.isNotEmpty()
        val usbOemExist = listUsbOem?.isNotEmpty()
        val onlyUsbOemOpened = (usbOemExist == true && listUsbOpened.size == listUsbOem.size)
        val onlyMagellanConfig = (listMagellanConfig?.isNotEmpty() == true && listUsbOpened.size == listMagellanConfig.size)

        // Tab-specific logic
        when (tabIndex) {
            1, 3, 4, 5 -> { // Configuration tab, Image capture tab, custom configuration, update firmware
                if (onlyUsbOemOpened) {
                    oemAlert = true
                    return false
                }
                if (isBluetoothEnabled.value == true) {
                    bluetoothAlert = true
                    return false
                }
                if (onlyMagellanConfig) {
                    magellanConfigAlert = true
                    return false
                }

                if (deviceSupportExist == false && listUsbOpened.isNotEmpty()) {
                    noDeviceSupportAlert = true
                    return false
                }

                setSelectedTabIndex(tabIndex)
                return true
            }

            2 -> { // DirectIO tab
                setSelectedTabIndex(tabIndex)
                return true
            }

            7, 8, 9, 10 -> {
                openAlert = false
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
                    Log.e(tag, "Error checking scale protocol", e)
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
                    Log.e(tag, "Error enabling scale protocol", e)
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

    // Utility to find a device (USB or BT) by our deviceId key
    // Find a USB device by our id key
    private fun findUsbById(deviceId: String): DatalogicDevice? =
        _deviceList.value?.firstOrNull { it.usbDevice.deviceId.toString() == deviceId }

    // Start scale for a specific USB deviceId
    fun startScaleHandler(deviceId: String) {
        val dev = findUsbById(deviceId) ?: run {
            // reflect failure to UI
            val cur = perDeviceScale.flowFor(deviceId).value
            perDeviceScale.emit(deviceId, cur.copy(status = "Device not found", seq = SystemClock.uptimeMillis()))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching { dev.startScale() }.getOrDefault(false)
            withContext(Dispatchers.Main) {
                perDeviceScale.setEnabled(deviceId, ok)
                val cur = perDeviceScale.flowFor(deviceId).value
                perDeviceScale.emit(
                    deviceId,
                    cur.copy(status = if (ok) "ENABLED" else "Failed to start scale", seq = SystemClock.uptimeMillis())
                )
                if (selectedDevice.value?.usbDevice?.deviceId.toString() == deviceId) {
                    _isEnableScale.postValue(ok)
                }
            }
        }
    }

    // Stop scale for a specific USB deviceId
    fun stopScaleHandler(deviceId: String) {
        val dev = findUsbById(deviceId) ?: run {
            val cur = perDeviceScale.flowFor(deviceId).value
            perDeviceScale.emit(deviceId, cur.copy(status = "Device not found", seq = SystemClock.uptimeMillis()))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching { dev.stopScale() }.getOrDefault(false)
            withContext(Dispatchers.Main) {
                perDeviceScale.setEnabled(deviceId, if (ok) false else perDeviceScale.enabledFlowFor(deviceId).value)
                val cur = perDeviceScale.flowFor(deviceId).value
                perDeviceScale.emit(
                    deviceId,
                    cur.copy(status = if (ok) "DISABLED" else "Failed to stop scale", seq = SystemClock.uptimeMillis())
                )
                if (selectedDevice.value?.usbDevice?.deviceId.toString() == deviceId) {
                    _isEnableScale.postValue(false)
                }
            }
        }
    }

    // Clear only the displayed scale data for a specific USB deviceId
    fun clearScaleData(deviceId: String) {
        perDeviceScale.clear(deviceId)
        if (selectedDevice.value?.usbDevice?.deviceId.toString() == deviceId) {
            _scaleStatus.postValue("")
            _scaleWeight.postValue("")
            _scaleUnit.postValue(ScaleUnit.NONE)
        }
    }

    // Convenient overloads when you already have the USB device object:
    fun startScaleHandler(device: DatalogicDevice) = startScaleHandler(device.usbDevice.deviceId.toString())
    fun stopScaleHandler(device: DatalogicDevice)  = stopScaleHandler(device.usbDevice.deviceId.toString())
    fun clearScaleData(device: DatalogicDevice)   = clearScaleData(device.usbDevice.deviceId.toString())

    fun toggleCheckDocking() {
        var enable = false
        if (isCheckDockingEnabled.value == true) {
            _isCheckDockingEnabled.postValue(enable)
        } else {
            enable = true
            _isCheckDockingEnabled.postValue(enable)
        }
        setAutoCheckDocking(enable)
    }
    fun setAutoCheckDocking(enable: Boolean) {
        Log.d("HomeViewModel", "[setAutoCheckDocking] enable: $enable")
        deviceList.value?.let {
            removeInvalidDeviceStates()
            for (device in deviceList.value!!) {
                if (!device.isDeviceSupportCheckDocking()) {
                    continue
                }
                if (enable == true) {
                    Log.d("HomeViewModel", "device ${device.displayName} startAutoCheckDocking")
                    val cradleEvent = object : CradleListener {
                        override fun onDockListener(docked: Boolean) {
                            updateDockStatus(device, docked)
                        }

                        override fun onLinkListener(linked: Boolean) {
                            updateLinkStatus(device, linked)
                        }
                    }
                    activeCradleListeners[device] = cradleEvent
                    device.registerCradleListener(cradleEvent)
                    device.startAutoCheckDocking()
                } else {
                    Log.d("HomeViewModel", "device ${device.displayName} stopAutoCheckDocking")
                    device.stopAutoCheckDocking()
                }
            }
        }
    }
    private fun updateDockStatus(device: DatalogicDevice, docked: Boolean) {
        _deviceCradleStates.update { list ->
            val existing = list.find { it.device == device }
            if (existing != null)
                list.map { if (it.device == device) it.copy(docked = docked) else it }
            else
                list + CradleState(device, docked = docked)
        }
    }
    private fun updateLinkStatus(device: DatalogicDevice, linked: Boolean) {
        _deviceCradleStates.update { list ->
            val existing = list.find { it.device == device }
            if (existing != null)
                list.map { if (it.device == device) it.copy(linked = linked) else it }
            else
                list + CradleState(device, linked = linked)
        }
    }
    fun removeInvalidDeviceStates() {
        val validDevices = deviceList.value ?: return
        val invalidDevices = _deviceCradleStates.value
            .map { it.device }
            .filter { device -> validDevices.none { it == device } }
        invalidDevices.forEach { device ->
            Log.d("HomeViewModel", "removeInvalidDeviceStates -> remove listener for ${device.displayName}")
            activeCradleListeners[device]?.let {
                device.unregisterCradleListener(it)
                activeCradleListeners.remove(device)
            }
        }
        _deviceCradleStates.update { list ->
            list.filter { state -> validDevices.any { it == state.device } }
        }
    }
    fun removeDeviceCradleState(usbDevice: UsbDevice) {
        val targetState = _deviceCradleStates.value.find { it.device.usbDevice == usbDevice } ?: return
        val targetDevice = targetState.device
        activeCradleListeners[targetDevice]?.let { listener ->
            targetDevice.unregisterCradleListener(listener)
            activeCradleListeners.remove(targetDevice)
            Log.d("HomeViewModel", "removeDeviceByUsb -> removed listener for ${targetDevice.displayName}")
        }
        _deviceCradleStates.update { list ->
            list.filter { it.device != targetDevice }
        }
    }

    fun saveConfigData(fileName: String) {
        if (!TextUtils.isEmpty(customConfiguration.value.toString()))
        // Save to file if fileName is provided
            if (fileName.isNotEmpty()) {
                selectedDevice.value?.let {
                    FileUtils.saveTextToDownloads(
                        context,
                        fileName,
                        customConfiguration.value.toString(), it.deviceType
                    )
                }
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

    fun clearConfig() {
        if (selectedDevice.value?.status?.value == DeviceStatus.CLOSED) {
            _customConfiguration.value = ""
            _readConfigData.postValue(hashMapOf())
        }
    }

    fun clearSelectedDevice(deviceName: String) {
        selectedDevice.value?.let {
            if (it.usbDevice.deviceName.toString() == deviceName) {
                setSelectedDevice(null)
            }
        }
    }

    fun upgradeFirmware(isBulkTransfer: Boolean = false) {
        _progressUpgrade.postValue(0)
        _isLoadingPercent.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            selectedDevice.value?.let {
                it.upgradeLoadedFirmware(
                    resetCallback = {
                        showResetDeviceDialog = true
                    },
                    progressCallback = { progress ->
                        run {
                            _progressUpgrade.postValue(progress)
                        }
                    }, isBulkTransfer, onFailure = { message ->
                        showResetDeviceDialog = false
                        showToast(context, message)
                    }
                )
                _isLoadingPercent.postValue(false)
            }
        }
    }

    fun loadFirmwareFile(
        file: File?,
        fileType: String,
        context: Context,
        onCompleteLoadFirmware: () -> Unit
    ) {
        _isLoadingPercent.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            selectedDevice.value?.let {
                it.loadFirmwareFile(
                    file!!, fileType, context,
                    onCompleteLoadFirmware = {
                        _isLoadingPercent.postValue(false)
                        onCompleteLoadFirmware()
                    }, progressCallback = { progress ->
                        run {
                            _progressUpgrade.postValue(progress)
                        }
                    })
            }
        }
    }

    fun getPid(file: File?, fileType: String, context: Context): String? {
        selectedDevice.value?.let {
            return it.getPid(file, fileType, context)
        }
        return ""
    }

    fun getPidDWF(file: File?, fileType: String) : String {
        selectedDevice.value?.let {
            val result = it.getPidDFW(file, fileType, context)
            if (result != null) {
                Log.d("HomeViewModel", "[getPidDWF] PID: $result")
                return result
            }
        }
        return ""
    }

    fun getBulkTransferSupported(onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            selectedDevice.value?.let {
                val supported = it.isBulkTransferSupported()
                _isBulkTransferSupported.postValue(supported)
                withContext(Dispatchers.Main) {
                    supported?.let { supported -> onResult(supported) }
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun setBulkTransferSupported(value: Boolean) {
        _isBulkTransferSupported.value = value
    }

    fun isFRS(): Boolean {
        return selectedDevice.value?.deviceType == DeviceType.FRS
    }

    fun setPid(file: File?, fileType: String, onResult: (Boolean) -> Unit, context: Context) {
        val result = selectedDevice.value?.isCheckPid(file, fileType, context) ?: false
        setCheckPid(result)
        onResult(result)
    }

    fun setPidDWF(file: File, fileType: String, onResult: (Boolean) -> Unit) {
        selectedDevice.value?.let {
            val result = it.isCheckPidDFW(file, fileType, context)
            if (result != null) {
                Log.d("HomeViewModel", "[setPidDWF] result: $result")
                setCheckPid(result)
                onResult(result)
            }
        }
    }

    private fun setCheckPid(value: Boolean) {
        _isCheckPid.value = value
    }

    // Log control functions
    fun toggleLogging() {
        if (_isLoggingEnabled.value == true) {
            logcatHelper.stop()
            _isLoggingEnabled.value = false
            Log.d(tag, "Logging stopped via UI toggle")
        } else {
            logcatHelper.start()
            _isLoggingEnabled.value = true
            Log.d(tag, "Logging started via UI toggle")
        }
    }

    fun toggleDebug() {
        if (_isDebugEnabled.value == true) {
            logcatHelper.setDebugMode(false)
            _isDebugEnabled.value = false
            Log.d(tag, "toggleDebug stopped via UI toggle")
            logcatHelper.exportLogsToPublicFolder(context)
        } else {
            logcatHelper.setDebugMode()
            _isDebugEnabled.value = true
            Log.d(tag, "toggleDebug started via UI toggle")
        }
    }

    fun saveLog() {
        val saveLog = logcatHelper.exportLogsToPublicFolder(context)
        if (saveLog) {
            showToast(context, "Save logs to /Download/AppLogs/logs.")
        } else {
            showToast(context, "Failed to save logs.")
        }
    }

    // REMOVED: saveLogsToFile() function - no longer needed since logs are only stored as SDK_log files
    // Output folder functionality has been removed

    fun initializeConnectTypeState() {
        _isBluetoothEnabled.value = false
    }

    fun toggleConnectionType(activity: Activity) {
        if (_isBluetoothEnabled.value == true) {
            _isBluetoothEnabled.value = false
            Log.d(tag, "[toggleConnectType] Show list USB device")
            if (status.value == DeviceStatus.OPENED) {
                //closeBluetoothDevice()
            }
            stopBluetoothPolling()
        } else if (getAllBluetoothDevice(activity)) {
            if (status.value == DeviceStatus.OPENED) {
                closeUsbDevice(selectedDevice.value)
            }
            _isBluetoothEnabled.value = true
            Log.d(tag, "[toggleConnectType] Show list Bluetooth device")
            if (selectedBluetoothDevice.value != null && allBluetoothDevices.value?.contains(selectedScannerBluetoothDevice.value) == true) {
                _status.postValue(DeviceStatus.CLOSED)
            }
//            startBluetoothPolling(activity)
        } else {
            Log.d(tag, "[toggleConnectType] getAllBluetoothDevice FAIL - Permission denied")
        }
    }

    private fun startBluetoothPolling(activity: Activity) {
        if (bluetoothPollingJob?.isActive == true) return
        bluetoothPollingJob = viewModelScope.launch {
            while (isActive) {
                Log.d(tag, "[startBluetoothPolling] Get all Bluetooth devices each 30s")
                delay(30_000)
                getAllBluetoothDevice(activity)
            }
        }
    }

    private fun stopBluetoothPolling() {
        Log.d(tag, "[stopBluetoothPolling] Stop getting all Bluetooth devices")
        bluetoothPollingJob?.cancel()
        bluetoothPollingJob = null
    }

    // REMOVED: saveLogsToFile() function - no longer needed since logs are only stored as SDK_log files
    // Output folder functionality has been removed

    fun initializeLoggingState() {
        _isLoggingEnabled.value = logcatHelper.isActive()
    }

    fun isSWUValid(file: File, context: Context): Boolean? {
        return selectedDevice.value?.isSWUFirmwareValid(file, context)
    }

    fun getAllBluetoothDevice(context: Activity) : Boolean {
        val result = usbDeviceManager.getAllBluetoothDevice(context) { devices ->
            _allBluetoothDevices.postValue(ArrayList(devices))
        }
        return result
    }

    fun createQrCode(profile: PairingBarcodeType, context: Activity) {
        val bluetoothProfile: BluetoothProfile = when (profile) {
            PairingBarcodeType.SPP -> BluetoothProfile.SPP
            PairingBarcodeType.HID -> BluetoothProfile.HID
            PairingBarcodeType.UNLINK -> return
        }
        val bitmap = usbDeviceManager.qrCodeGenerator(context, bluetoothProfile)
        val scaledBitmap = bitmap.scale(210, 210, false)
        _qrBitmap.value = scaledBitmap
        setPreviousBluetoothProfile(PairingBarcodeType.UNLINK)
        currentPairingStatus.value = PairingStatus.Scanning
        scanBluetoothDevice(context)
    }

    fun scanBluetoothDevice(context: Activity) {
        Log.d(tag, "[scanBluetoothDevice] stopScanBluetoothDevices")
        usbDeviceManager.stopScanBluetoothDevices(context)
        usbDeviceManager.scanBluetoothDevices(context) { pairingData ->
            val status = pairingData.pairingStatus
            val message = pairingData.message
            val name = pairingData.deviceName

            when (status) {
                BluetoothPairingStatus.Successful -> {
                    if (message.contains("connected")) {
                        setPairingStatus(PairingStatus.Connected)
                    } else {
                        setPairingStatus(PairingStatus.Paired)
                    }
                    getAllBluetoothDevice(context)
                }

                BluetoothPairingStatus.Unsuccessful -> {
                    if (message == "Permission denied") {
                        setPairingStatus(PairingStatus.PermissionDenied)
                    } else {
                        setPairingStatus(PairingStatus.Error)
                    }
                }

                BluetoothPairingStatus.Timeout -> {
                    setPairingStatus(PairingStatus.Timeout)
                }
            }
            currentBleDeviceName.value = pairingData.deviceName
            Log.d(
                tag,
                "[scanBluetoothDevice] scan device ${pairingData.deviceName} ${pairingData.pairingStatus} : ${pairingData.message}"
            )
        }
    }

    fun coroutineOpenBluetoothDevice(device: DatalogicBluetoothDevice, context: Activity) {
        // A) Avoid duplicate opens
        if (device.status.value == DeviceStatus.OPENED) {
            showToast(context, "Device already opened")
            return
        }

        // C) Select the *same instance* you are opening
        setSelectedBluetoothDevice(device)

        // Fresh scan listener every time
        bluetoothScanEvent = object : UsbScanListener {
            override fun onScan(scanData: UsbScanData) {
                Log.d(tag, "[bluetoothScanEvent] onScan data: ${scanData.barcodeData}")
                emitScanFrom(device.bluetoothDevice.address, scanData)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.d(tag, "[coroutineOpenBluetoothDevice] connectDevice")
            val listener = bluetoothScanEvent ?: return@launch

            // D) Give the OS a beat in case we just closed the link (prevents busy GATT/RFCOMM)
            delay(150)

            device.connectDevice(listener, context) { status ->
                viewModelScope.launch(Dispatchers.Main) {
                    if (status == BluetoothPairingStatus.Successful) {
                        bluetoothErrorListener = object : UsbDioListener {
                            override fun fireDioErrorEvent(errorCode: Int, message: String) {
                                showToast(context, message + errorCode)
                            }
                        }
                        bluetoothErrorListener?.let { device.registerBluetoothDioListener(it) }

                        _status.postValue(DeviceStatus.OPENED)
                        device.status.value = DeviceStatus.OPENED
                        _deviceStatus.postValue("Device opened status ${device.status.value}")
                        updateBluetoothStatusInList(device, DeviceStatus.OPENED)
                        selectedScannerBluetoothDevice.postValue(device)
                        val cmd = DIOCmdValue.ENABLE_SCANNER
                        device.dioCommand(cmd, cmd.value, context)
                        showToast(context, "Device successfully opened")
                    } else {
                        _status.postValue(DeviceStatus.CLOSED)
                        device.status.value = DeviceStatus.CLOSED
                        _deviceStatus.postValue("No device selected")
                        updateBluetoothStatusInList(device, DeviceStatus.CLOSED)
                    }
                }
            }
        }
    }

    fun updateBluetoothStatusInList(target: DatalogicBluetoothDevice, newStatus: DeviceStatus) {
        val list = _allBluetoothDevices.value ?: return

        val idx = list.indexOfFirst { dev ->
            dev.bluetoothDevice.address == target.bluetoothDevice.address
        }
        if (idx >= 0) {
            val dev = list[idx]
            // update the device’s own status LiveData
            dev.status.value = newStatus
            // re-post a new list instance to trigger observers
            _allBluetoothDevices.postValue(ArrayList(list))
        }
    }

    fun closeBluetoothDevice(dlBluetoothDevice: DatalogicBluetoothDevice?) {
        val dev = dlBluetoothDevice ?: return
        // detach listeners
        bluetoothErrorListener?.let { dev.unregisterBluetoothDioListener(it) }
        dev.clearConnection(context)
        viewModelScope.launch(Dispatchers.IO) { delay(150) }
        // status → CLOSED
        dev.status.value = DeviceStatus.CLOSED
        _status.postValue(DeviceStatus.CLOSED)
        _deviceStatus.postValue("No device selected")
        updateBluetoothStatusInList(dev, DeviceStatus.CLOSED)

        // clear per-device UI
        perDeviceClear(dev)
        selectedScannerBluetoothDevice.postValue(null)
        // avoid reusing stale listeners on next open
        bluetoothScanEvent = null
        bluetoothErrorListener = null
    }

    fun setSelectedBluetoothDevice(device: DatalogicBluetoothDevice?) {
        device?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(tag, "[setSelectedBluetoothDevice] do not have permission")
                _status.postValue(DeviceStatus.NONE)
            }
            _deviceStatus.postValue("Selected: ${device.name}")
        } ?: run {
            _deviceStatus.postValue("No device selected")
        }
        selectedBluetoothDevice.value = device
//        selectedDevice.value = null
        selectedUsbDevice.value = null
    }

    // In HomeViewModel.kt

    /**
     * Checks the current open devices and sets a default selected device
     * ONLY if no device is currently selected.
     * This prevents overriding a user's manual selection.
     */
    fun setDefaultDevice() {
        // Only proceed if no device is selected at all
        if (selectedDevice.value == null && selectedBluetoothDevice.value == null) {
            val openUsbDevices = _deviceList.value?.filter { it.status.value == DeviceStatus.OPENED }
            val openBluetoothDevices = _allBluetoothDevices.value?.filter { it.status.value == DeviceStatus.OPENED }

            if (!openUsbDevices.isNullOrEmpty()) {
                setSelectedDevice(openUsbDevices[0])
                Log.d("HomeViewModel", "Default device set to USB: ${openUsbDevices[0].displayName}")
            } else if (!openBluetoothDevices.isNullOrEmpty()) {
                setSelectedBluetoothDevice(openBluetoothDevices[0])
                Log.d("HomeViewModel", "Default device set to Bluetooth: ${openBluetoothDevices[0].name}")
            }
        }
    }

    // In HomeViewModel.kt

    /**
     * Iterates through all known devices (USB and Bluetooth) and ensures they are closed.
     * This is intended to be called when the application is shutting down.
     */
    fun closeAllDevices() {
        Log.d(tag, "Closing all connected devices...")

        // Close all USB devices
        _deviceList.value?.forEach { device ->
            if (device.status.value == DeviceStatus.OPENED) {
                Log.d(tag, "Closing USB device: ${device.displayName}")
                // Use your existing closeDevice logic if it handles all cleanup.
                // Assuming 'closeDevice' in DatalogicDevice handles listeners and connections.
                closeUsbDevice(device)
            }
        }

        // Close all Bluetooth devices
        _allBluetoothDevices.value?.forEach { device ->
            if (device.status.value == DeviceStatus.OPENED) {
                Log.d(tag, "Closing Bluetooth device: ${device.name}")
                closeBluetoothDevice(device) // Assuming DatalogicBluetoothDevice has a close() method.
            }
        }

        // Clear the lists after closing
        _deviceList.postValue(ArrayList())
        _allBluetoothDevices.postValue(ArrayList())

        Log.d(tag, "All devices have been instructed to close.")
    }


    fun setSelectedBluetoothProfile(profile: PairingBarcodeType) {
        selectedBluetoothProfile.value = profile
    }

    fun setPreviousBluetoothProfile(profile: PairingBarcodeType?) {
        previousBluetoothProfile.value = profile
    }

    fun setPairingStatus(status: PairingStatus?) {
        currentPairingStatus.value = status
    }

    /**
     * Set the selected label code type and sync with the device
     * @param labelCodeType The label code type to set
     */
    fun setSelectedLabelCodeType(labelCodeType: LabelCodeType) {
        _selectedLabelCodeType.value = labelCodeType

        // Sync with the device if it's available and opened
        selectedDevice.value?.let { device ->
            if (device.status.value == DeviceStatus.OPENED) {
                device.setCurrentLabelCodeType(labelCodeType)
                Log.d(tag, "Label code type synced with device: ${labelCodeType.code}")
            }
        }
    }

    /**
     * Set the selected label ID control and sync with the device
     * @param labelIDControl The label ID control to set
     */
    fun setSelectedLabelIDControl(labelIDControl: LabelIDControl) {
        _selectedLabelIDControl.value = labelIDControl

        // Sync with the device if it's available and opened
        selectedDevice.value?.let { device ->
            if (device.status.value == DeviceStatus.OPENED) {
                device.setCurrentLabelIDControl(labelIDControl)
                Log.d(tag, "Label ID control synced with device: ${labelIDControl.code}")
            }
        }
    }

    /**
     * Get the current label code type setting from the device
     * @return Current LabelCodeType value
     */
    fun getCurrentLabelCodeType(): LabelCodeType {
        return selectedDevice.value?.getCurrentLabelCodeType() ?: LabelCodeType.NONE
    }

    /**
     * Get the current label ID control setting from the device
     * @return Current LabelIDControl value
     */
    fun getCurrentLabelIDControl(): LabelIDControl {
        return selectedDevice.value?.getCurrentLabelIDControl() ?: LabelIDControl.DISABLE
    }

    /**
     * Initialize label settings from the device when it's opened
     * This should be called when device is successfully opened
     */
    private fun initializeLabelSettingsFromDevice() {
        selectedDevice.value?.let { device ->
            if (device.status.value == DeviceStatus.OPENED) {
                val deviceLabelCodeType = device.getCurrentLabelCodeType()
                val deviceLabelIDControl = device.getCurrentLabelIDControl()

                _selectedLabelCodeType.postValue(deviceLabelCodeType)
                _selectedLabelIDControl.postValue(deviceLabelIDControl)

                Log.d(
                    tag,
                    "Label settings initialized from device - CodeType: ${deviceLabelCodeType.code}, IDControl: ${deviceLabelIDControl.code}"
                )
            }
        }
    }

    fun getAppInfo() {
        val osName = "Android"
        val osVersion = Build.VERSION.RELEASE
        val sdkInt = Build.VERSION.SDK_INT
        val deviceModel = Build.MODEL
        val deviceBrand = Build.BRAND
        val arch = System.getProperty("os.arch") ?: "unknown"
        val timeZone = java.util.TimeZone.getDefault().id
        val versionSDK = BuildConfig.LIBRARY_VERSION_NAME
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val appVersion = versionSDK.removePrefix("AndroidSDK_")

        Log.i("AppInfo", "App Version: $appVersion")
        Log.i("AppInfo", "Version SDK: $versionSDK")
        Log.i("AppInfo", "Device OS: $osName $osVersion (SDK $sdkInt; $arch; $deviceBrand $deviceModel)")
        Log.i("AppInfo", "Time zone: $timeZone")

        aboutApp.postValue(
            AboutModel(
                osName, osVersion, sdkInt, deviceModel,
                deviceBrand, arch, timeZone, versionSDK, appVersion
            )
        )
    }

    fun setMsgConfigError(message: String) {
        _msgConfigError.postValue(message)
    }

    fun enableScannerBeforeCapturing(device: DatalogicDevice?) {
        val command = DIOCmdValue.ENABLE_SCANNER
        Log.d("HomeViewModel", "Enable scanner ...")
        device?.dioCommand(command, command.value, context)
    }
}

