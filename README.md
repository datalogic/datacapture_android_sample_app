# Datalogic Android SDK — Design Document (v2.4.4)

**Last Saved:** 17/12/2025

---

## Table of Contents
- [1. Overview](#overview)
- [2. Namespace](#namespace)
- [3. Supported Device](#supported-device)
  - [3.1. USB-OEM](#usb-oem)
  - [3.2. USB-COM](#usb-com)
  - [3.3. USB-COM-SC](#usb-com-sc)
  - [3.4. Bluetooth (SPP profile)](#bluetooth-spp-profile)
  - [3.5. Bluetooth (HID profile)](#bluetooth-hid-profile)
- [4. Special setting and limitation](#special-setting-and-limitation)
  - [4.1. Dependencies setting to use Android SDK](#dependencies-setting-to-use-android-sdk)
  - [4.2. Special setting for Magellan](#special-setting-for-magellan)
  - [4.3. Firmware upgrade and Custom configuration for the old Magellan device](#firmware-upgrade-and-custom-configuration-for-the-old-magellan-device-magellan-1500i-magellan-9800i-magellan-34xx-magellan-35xx)
  - [4.4. Limitation of the reset command with Magellan 9600i and 9900i](#limitation-of-the-reset-command-with-magellan-9600i-and-9900i)
- [5. Interfaces](#interfaces)
  - [5.1. UsbListener](#usblistener)
  - [5.2. UsbScanListener](#usbscanlistener)
  - [5.3. StatusListener](#statuslistener)
  - [5.4. UsbDioListener](#usbdiolistener)
  - [5.5. UsbScaleListener](#usbscalelistener)
  - [5.6. BluetoothListener](#bluetoothlistener)
  - [5.7. CradleListener](#cradlelistener)
- [6. Classes](#classes)
  - [6.1. DatalogicDeviceManager](#datalogicdevicemanager)
  - [6.2. DatalogicDevice](#datalogicdevice)
  - [6.3. DatalogicBluetoothDevice](#datalogicbluetoothdevice)
  - [6.4. UsbScanData](#usbscandata)
- [7. Enums](#enums)
- [8. Sample code (Kotlin)](#sample-code-kotlin)
  - [8.1. USB Scanner](#usb-scanner)
    - [8.1.1. Detect the connected device list](#detect-the-connected-device-list)
    - [8.1.2. Open the connected device and setup the scan listener to get the barcode scanning data](#open-the-connected-device-and-setup-the-scan-listener-to-get-the-barcode-scanning-data)
  - [8.2. Bluetooth Scanner](#bluetooth-scanner)
    - [8.2.1. Steps to pair Bluetooth Scanner with SPP profile](#steps-to-pair-bluetooth-scanner-with-spp-profile)
    - [8.2.2. Steps to connect Bluetooth Scanner to receive scanning data](#steps-to-connect-bluetooth-scanner-to-receive-scanning-data)

---

## Overview
The Datacapture SDK allows developers to write Android apps using both Java and Kotlin languages that can connect to handheld scanners and fixed retail scanners.

## Namespace
The Datacapture SDK has the namespace `com.datalogic.aladdin.aladdinusbscannersdk`.

## Supported Device

### USB-OEM

**Legend:** ✅ Supported • ❌ Not Supported

| Product | Event notification (connect / disconnect) | Display barcode data | BEEPER | Scanning control (DIO Command) | Asset tracking (DIO Command) | Enable/Disable symbologies | I/O command to take a picture | Scale support | Custom Configuration | Firmware Upgrade |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Gryphon GD45xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Gryphon GBT45xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Gryphon GFS45xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| QuickScan QD25xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| PowerScan PD96xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| PowerScan PBT96xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 900i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 9900i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Magellan 9600i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Magellan 1500i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 9800i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Magellan 34xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 35xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 9550i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |

### USB-COM

**Legend:** ✅ Supported • ❌ Not Supported

| Product | Event notification (connect / disconnect) | Display barcode data | BEEPER | Scanning control (DIO Command) | Asset tracking (DIO Command) | Enable/Disable symbologies | I/O command to take a picture | Scale support | Custom Configuration | Firmware Upgrade |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Gryphon GD45xx | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Gryphon GBT45xx | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Gryphon GFS45xx | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| QuickScan QD25xx | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| PowerScan PD96xx | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| PowerScan PBT96xx | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Magellan 900i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ✅ | ✅ |
| Magellan 9900i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Magellan 9600i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Magellan 1500i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 9800i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Magellan 34xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 35xx | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Magellan 9550i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### USB-COM-SC

**Legend:** ✅ Supported • ❌ Not Supported

| Product | Event notification (connect / disconnect) | Display barcode data | BEEPER | Scanning control (DIO Command) | Asset tracking (DIO Command) | Enable/Disable symbologies | I/O command to take a picture | Scale support | Custom Configuration | Firmware Upgrade |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| Magellan 9900i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Magellan 9600i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Magellan 9800i | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| Magellan 9550i | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

### Bluetooth (SPP profile)

**Legend:** ✅ Supported • ❌ Not Supported

| Product | Minimum FW Version | Pair | Connect/Disconnect | Display barcode data | DIO command |
|---|---|:---:|:---:|:---:|:---:|
| QuickScan 2500 BT | 610171631 | ✅ | ✅ | ✅ | ❌ |
| PowerScan 9600 BT | 610149767 | ✅ | ✅ | ✅ | ✅ |
| PowerScan 9600 AR BT | 610149767 | ✅ | ✅ | ✅ | ✅ |
| PowerScan 9600 RFID BT | 610260201 | ✅ | ✅ | ✅ | ✅ |

### Bluetooth (HID profile)

**Legend:** ✅ Supported • ❌ Not Supported

| Product | Minimum FW Version | Pair |
|---|---|:---:|
| QuickScan 2500 BT | 610171631 | ✅ |
| PowerScan 9600 BT | 610149767 | ✅ |
| PowerScan 9600 AR BT | 610149767 | ✅ |
| PowerScan 9600 RFID BT | 610260201 | ✅ |

## Special setting and limitation

### Dependencies setting to use Android SDK
Modify `build.gradle.kts` (application gradle): Add the following lines to your app's dependencies block.

```kotlin
// ... other dependencies
implementation("com.google.code.gson:gson:2.13.2")
implementation("com.github.mik3y:usb-serial-for-android:3.9.0")
implementation("uk.org.okapibarcode:okapibarcode:0.5.2")
implementation("com.caverock:androidsvg:1.4")
```

Modify `settings.gradle.kts`: Ensure the JitPack repository is included in both `pluginManagement` and `dependencyResolutionManagement` blocks.

```kotlin
pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://jitpack.io") }
  }
}

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
  }
}
```

### Special setting for Magellan

| Device | Setting |
|---|---|
| - Magellan 9900i/9600i<br>- Magellan 900i<br>- Magellan 9550i | - USB-COM must disable CDC control endpoint (by setting disable value for the service command 074B)<br>- USB-COM-SC must be configured that RTS is held in low state and CTS is ignored (by setting the command 047B to value 00) |
| - Other Magellan device | - When connect by USB-COM or USB-COM-SC interface, the command 0550 must disable CDC control endpoint (set bit 4 to the value 1 - Block transmission of messages over control endpoint) |

### Firmware upgrade and Custom configuration for the old Magellan device (Magellan 1500i, Magellan 9800i, Magellan 34xx, Magellan 35xx)

| Device | Setting |
|---|---|
| - Magellan 1500i<br>- Magellan 9800i<br>- Magellan 34xx<br>- Magellan 35xx | On the section “3. Supported devices”, it shows that Magellan 1500i, Magellan 9800i, Magellan 34xx, Magellan 35xx do not support for Firmware upgrade and Custom configuration with the normal setting. However, we can execute the firmware upgrade and custom configuration with the service cable (or service mode). Please plug the properly service cable or switch device to the service mode before use those features. |

### Limitation of the reset command with Magellan 9600i and 9900i

| Device | Setting |
|---|---|
| Magellan 9900i/9600i | The reset command of Magellan 9600i and 9900i can work properly on almost Android device. However, we observe the reset issue with Samsung Tablet, Memor 12/17/30/35, Joya Smart . After executing the reset command, Android SDK cannot open device again. The user needs to unplug and plug the USB cable again to recover USB connection |

## Interfaces

The SDK will have a series of interfaces that expose abstract methods that can be overwritten by the user to receive callbacks to events.

| Interfaces | Description |
|---|---|
| UsbListener | For listening to USB Scanner device connection events (connected or disconnected). |
| UsbScanListener | For listening to USB Scan events. |
| StatusListener | For listening to when a device status get changed |
| UsbDioListener | For listening to when a DIO Commands get execute. |
| UsbScaleListener | For listening to USB scale events that will return the scale data. |
| BluetoothListener | For listening to Bluetooth Scanner device connection events (attached or detached). |

### UsbListener

Interface with an abstract method that is triggered when a USB Scanner device is connected or disconnected with the mobile device.

| Public Abstract Methods | Description |
|---|---|
| `fun onDeviceAttachedListener()` | Called when a USB Scanner connection event is fired. |
| `fun onDeviceDetachedListener()` | Called when a USB Scanner disconnection event is fired. |

### UsbScanListener

Interface with a single abstract method that is triggered when a USB Scanner device scans a barcode.

| Public Abstract Methods | Description |
|---|---|
| `fun onScan(scanData: UsbScanData)` | Called when a USB Scanner scan event is fired. Receives ScanData of the scanned barcode as an argument. |

### StatusListener

Interface with abstract method that is triggered when a when a device status get changed.

| Public Abstract Methods | Description |
|---|---|
| `fun onStatus(productId:String, status: DeviceStatus)` | Called when a USB device status event is fired. |
| `fun onError(errorStatus : Int)` | Function called when error occurred. |

### UsbDioListener

Interface with abstract method that is triggered when a DIO Commands get failed.

| Public Abstract Methods | Description |
|---|---|
| `fun fireDioErrorEvent(errorCode: Int, message: String)` | Called when a DIO Commands get failed. |

### UsbScaleListener

Interface with a single abstract method that is triggered when a USB Scanner device returns the scale value.

| Public Abstract Methods | Description |
|---|---|
| `fun onScale(scaleData: ScaleData)` | Called when a USB Scanner scale event is fired. Receives ScaleData of the scale data as an argument. |

### BluetoothListener

Interface with an abstract method that is triggered when a Bluetooth Scanner device is attached or detached with the mobile device.

| Public Abstract Methods | Description |
|---|---|
| `fun onDeviceAttachedListener()` | Called when a Bluetooth Scanner attached event is fired. |
| `fun onDeviceDetachedListener()` | Called when a USB Scanner detached event is fired. |

### CradleListener

Interface with an abstract method that is triggered when the Cradle device has the docking status notification to the mobile device.

| Public Abstract Methods | Description |
|---|---|
| `fun onDockListener(docked: Boolean)` | Called when a scanner is docked/undocked on a cradle. |

## Classes

The following classes are required for the SDK.

| Classes | Description |
|---|---|
| DatalogicDeviceManager | Class manages the attached Datalogic Scanner. |
| DatalogicDevice | Class represents for Datalogic Scanner. It contains the Datalogic Scanner details and all functions to interact with Datalogic Scanner |
| DatalogicBluetoothDevice | Class represents for Datalogic Bluetooth Scanner. It contains the Datalogic Bluetooth Scanner details and all functions to interact with Datalogic Bluetooth Scanner |
| `UsbScanData( val rawData: ByteArray,  val barcodeData: String,  val barcodeType: String )` | Data class passed by onScan to retrieve scanned data from the USB Scanner. |
| `ScaleData( val status: String,  val weight: String,  val unit: ScaleUnit )` | Data class passed by onScale to retrieve scale data from the USB Scanner. |

### DatalogicDeviceManager

Management class for the Datalogic Scanner device. Used to detect USB Scanner Devices and register scanner events. USB permission is required for communicate with USB devices.

| Public Methods | Description |
|---|---|
| `ArrayList<DatalogicDevice> detectDevice(context: Context)` | get the connected devices list and prepare the DatalogicDevice based on the productId. Return the connected device list of ArrayList<DatalogicDevice>. |
| `Int registerUsbListener(listener: UsbListener)` | Register a listener for USB Events to notify when the USB Scanner connected or disconnected. |
| `Int unregisterUsbListener(listener: UsbListener)` | Unregister listener for USB Events. |
| `Int registerStatusListener(listener: StatusListener)` | Register listener for device Status Changes |
| `Int unregisterStatusListener(listener: StatusListener)` | Unregistered listeners for Status event. |
| `Bitmap qrCodeGenerator(context: Context, bluetoothProfile: BluetoothProfile)` | Generate the barcode image to pair Bluetooth device. Scanner will need to scan this barcode when we want to pair it to Android device through Bluetooth connection. |
| `Void scanBluetoothDevices(context: Activity, onComplete: (BluetoothPairingData) -> Unit)` | Start scan Bluetooth scanner device (that have scanned the barcode from qrCodeGenerator) to pair with it. The pair result is returned on BluetoothPairingData. |
| `Void stopScanBluetoothDevices(context: Context)` | Remove Bluetooth device scanning setup. Can use before execute scanBluetoothDevices to make sure that all setup is set to default. |
| `Boolean getAllBluetoothDevice(context: Context, onComplete: (ArrayList<BluetoothDevice>) -> Unit)` | Get all current Bluetooth device. The Bluetooth devices are returned in ArrayList<BluetoothDevice>). Note: it includes not only Datalogic Scanner device but also all Bluetooth device. |

### DatalogicDevice

Class represents for Datalogic Scanner. It contains the Datalogic Scanner details and all functions to interact with Datalogic Scanner

| Public Methods | Description |
|---|---|
| `Int openDevice(context: Context)` | This method opens a connection to the specified DatalogicDevice, allowing for communication with the device’s endpoints. Return 0 if the interface was successfully opened, -1 otherwise. |
| `Int closeDevice()` | Close the DatalogicDevice. |
| `Int registerUsbScanListener(listener: UsbScanListener)` | Register listener for USB scan event to be notified when the Scanner scans the barcodes. |
| `Int unregisterUsbScanListener(listener: UsbScanListener\)` | Unregister listeners for Scan event. |
| `Int registerUsbDioListener(listener: UsbDioListener)` | Register listener for Usb dio event to be notified when the Dio Cammands get failed. |
| `Int unregisterUsbDioListener(listener: UsbDioListener)` | Unregistered listeners for dio error event. |
| `String dioCommand(commandType: DIOCmdValue, cmd: String, context: Context)` | Function to execute the DIO Commands and return _status value(Command type and valid command value should be correct for output)_ |
| `HashMap<ConfigurationFeature, String> readConfig()` | Function to execute the read config Commands. |
| `HashMap<ConfigurationFeature, String> writeConfig(data: HashMap<ConfigurationFeature, String>)` | Function to execute the write config Commands. |
| `ByteArray imageCaptureAuto( currentBrightness,  currentContrast )` | Function to capture image. The parameter currentBrightness and currentContrast only is applied for HHS device |
| `Boolean startScale()` | Start to receive scale data |
| `Boolean stopScale()` | Stop to receive scale data |
| `Boolean isScaleAvailable()` | Check if this device is supported scale or not |
| `String getCustomConfiguration()` | Get all current configurations on device |
| `ConfigurationResult writeCustomConfiguration(configStr: String)` | Write all configurations in configStr into device |
| `Boolean upgradeFirmware( file: File,  fileType: String,  context: Context,  resetCallback: () -> Unit,  progressCallback: (Int) -> Unit,  isBulkTransfer: Boolean,  onFailure: (String) -> Unit )` | Function to upgrade firmware for device. `file`: Path to firmware file on local folder. `fileType`: can be “DFW”, “SWU”, “S37”. `isBulkTransfer`: set to “true” to use Bulk Transfer protocol to speed up firmware upgrade. Only support “S37” on Magellan 9600i, 900i, 9550i. The `upgradeFirmware()` function actually contains the `loadFirmwareFile()` and `upgradeLoadedFirmware()` function |
| `Void loadFirmwareFile( file: File,  fileType: String,  context: Context,  onCompleteLoadFirmware: () -> Unit,  progressCallback: (Int) -> Unit )` | Function to load the firmware file into sdk. It is usually used with the firmware file .dfw because the big size .dfw file will spend a long time to be loaded into sdk. |
| `Boolean upgradeLoadedFirmware( resetCallback: () -> Unit,  progressCallback: (Int) -> Unit,  isBulkTransfer: Boolean,  onFailure: (String) -> Unit )` | Function to upgrade the loaded firmware which had been loaded by the `loadFirmwareFile ()` function |
| `Boolean isSupportCheckDocking()` | Check if this device can notify the docking status |
| `Void startAutoCheckDocking()` | Start automatically checking the docking status and notify through the `onDockListener` function |
| `Void stopAutoCheckDocking()` | Stop automatically checking the docking status and notify through the `onDockListener` function |

### DatalogicBluetoothDevice

Class represents for Datalogic Bluetooth Scanner. It contains the Datalogic Bluetooth Scanner details and all functions to interact with Datalogic Bluetooth Scanner

| Public Methods | Description |
|---|---|
| `DatalogicBluetoothDevice( bluetoothDevice: BluetoothDevice,  bluetoothProfile: BluetoothProfile,  hostType: HostType )` | To create Bluetooth SPP scanner: `DatalogicBluetoothDevice(device, BluetoothProfile.SPP, HostType.Unconfigured)` |
| `Void connectDevice( serialListener: UsbScanListener,  context: Activity,  onComplete: (BluetoothPairingStatus) -> Unit )` | Connect the Bluetooth scanner |
| `Void clearConnection(context: Context)` | Disconnect the Bluetooth scanner |
| `String dioCommand( commandType: DIOCmdValue,  cmd: String, context: Context )` | Function to execute the DIO Commands and return _status value(Command type and valid command value should be correct for output)_ |

### UsbScanData

Data class containing the data of a scanned code. Can retrieve barcode data either as a String or an array if bytes.

| Public Properties﻿ | Description |
|---|---|
| `byte[] rawData` | Get the barcode data as an array of bytes. |
| `String barcodeData` | Get the barcode data as a String. |
| `String barcodeType` | Get the barcode ID as a String. |

## Enums

The following Enum properties are required for the SDK.

| Enums | Values | Description |
|---|---|---|
| BarcodeType | `[ EAN_JAN_13("0x16", "EAN/JAN-13"), EAN_JAN_8("0x0C", "EAN/JAN-8"), UPC_A("0x0D", "UPC-A"), UPC_E("0x0A", "UPC-E"), UPC_D1("0x0011", "UPC-D1"), UPC_D2("0x0012", "UPC-D2"), UPC_D3("0x0014", "UPC-D3"), UPC_D4("0x0017", "UPC-D4"), UPC_D5("0x001D", "UPC-D5"), UPC_A_2("0x00160B", "UPC-A+2"), UPC_A_5("0x00110B", "UPC-A+5"), UPC_E_2("0x00120B", "UPC-E+2"), UPC_E_5("0x00140B", "UPC-E+5"), EAN_JAN_8_2("0x00170B", "EAN/JAN-8+2"), EAN_JAN_8_5("0x001D0B", "EAN/JAN-8+5"), EAN_JAN_13_2("0x00130B", "EAN/JAN-13+2"), EAN_JAN_13_5("0x00150B", "EAN/JAN-13+5"), STANDARD_2_OF_5("0x000C0B", "Standard (Discrete) 2 of 5"), INTERLEAVED_2_OF_5("0x000D0B", "Interleaved 2 of 5"), CODE39("0x000A0B", "Code39"), CODE128("0x00180B", "Code128"), CODABAR("0x000E0B", "Codabar"), CODE93("0x00190B", "Code 93"), UCC_EAN_128("0x00250B", "UCC/EAN-128"), UPC_A_CODE128_SUPPLEMENTAL("0x00200B", "UPC-A w/Code 128 Supplemental"), UPC_E_CODE128_SUPPLEMENTAL("0x00210B", "UPC-E w/Code 128 Supplemental"), EAN_JAN_8_CODE128_SUPPLEMENTAL("0x00220B", "EAN/JAN-8 w/Code 128 Supplemental"), EAN_JAN_13_CODE128_SUPPLEMENTAL("0x00230B", "EAN/JAN-13 w/Code 128 Supplemental"), GS1_DATABAR("0x002A0B", "GS1 DataBar/GS1 DataBar Limited/GS1 DataBar Stacked"), GS1_DATABAR_EXPANDED("0x002B0B", "GS1 DataBar Expanded"), PDF417("0x002E0B", "PDF417"), MICROPDF("0x00380B", "MicroPDF"), MAXICODE("0x002F0B", "Maxicode"), OCR_A("0x00300B", "OCR-A"), OCR_B("0x00310B", "OCR-B"), DATAMATRIX("0x00320B", "DataMatrix"), GS1_DATAMATRIX("0x00360B", "GS1 DataMatrix"), QR_CODE("0x00330B", "QR Code"), GS1_QR_CODE("0x00370B", "GS1 QR Code"), AZTEC_CODE("0x00340B", "Aztec Code"), CODE49("0x00350B", "Code 49"), UNKNOWN("0x00FF0B", "Unknown") ]` | Options for the ability of the reader to translate Barcode Type. |
| DeviceStatus | `[CLOSED ,OPENED, NONE]` | Connection and Interface state. |
| DIOCmdValue | *(see SDK source for full byte arrays)* | DIO Commands and values for both COM and OEM devices. |
| ConfigurationFeature | *(various keys such as `CC3EN`, `CC8EN`, `C8BEN`, etc.)* | Configuration Read and write commands for COM Devices |
| ScaleUnit | `[KG, LB, NONE]` | Scale Unit |

## Sample code (Kotlin)

### USB Scanner

#### Detect the connected device list

```kotlin
var usbDeviceManager: DatalogicDeviceManager
usbDeviceManager.detectDevice(context) { devices ->
  _deviceList.postValue(ArrayList(devices))
}
```

#### Open the connected device and setup the scan listener to get the barcode scanning data

```kotlin
// Open the device
val result = device.openDevice(context)
when (result) {
  USBConstants.SUCCESS -> {
    //Setup listener
    scanEvent = object : UsbScanListener {
      //The onScan() function will be called when scanner scan the barcode
      override fun onScan(scanData: UsbScanData) {
        setScannedData(scanData)
      }
    }
  }
}
```

### Bluetooth Scanner

#### Steps to pair Bluetooth Scanner with SPP profile

```kotlin
var usbDeviceManager: DatalogicDeviceManager
// Create the barcode image to pair scanner
val bitmap = usbDeviceManager.qrCodeGenerator(context, BluetoothProfile.SPP)
// Start to pair the Bluetooth scanner
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
}
// We need to scan the bitmap image with Bluetooth scanner here…
// Remove Bluetooth device scanning setup
usbDeviceManager.stopScanBluetoothDevices(context)
```

#### Steps to connect Bluetooth Scanner to receive scanning data

```kotlin
// Create the scanning listener 
bluetoothScanEvent = object : UsbScanListener {
  override fun onScan(scanData: UsbScanData) {
    setScannedData(scanData)
  }
}

// Connect the Bluetooth scanner 
CoroutineScope(Dispatchers.IO).launch {
  device.connectDevice(bluetoothScanEvent, context) { status ->
    if (status == BluetoothPairingStatus.Successful) {
      _status.postValue(DeviceStatus.OPENED)
      _deviceStatus.postValue("Device opened")
    } else {
      _status.postValue(DeviceStatus.CLOSED)
      _deviceStatus.postValue("No device selected")
    }
  }
}

// When the scanner scan barcode, the scanning data should be returned on bluetoothScanEvent
//…

// Disconnect the Bluetooth scanner
device.clearConnection(context)
```

---

*Datalogic INTERNAL*
