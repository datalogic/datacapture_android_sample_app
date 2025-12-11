package com.datalogic.aladdin.aladdinusbapp.views.activity.updateFirmware

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.FileUtils
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ReleaseInformationCard
import com.datalogic.aladdin.aladdinusbapp.views.compose.UpgradeConfigurationCard
import com.datalogic.aladdin.aladdinusbapp.views.compose.UsbBTDeviceDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.constants.FileConstants
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import java.io.File

@Preview(showBackground = true)
@Composable
fun UpdateFirmwareScreen() {
    val homeViewModel = LocalHomeViewModel.current
    val isLoadFile = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var isCheckPidToggle by remember { mutableStateOf(true) }
    var isBulkTransferToggle by remember { mutableStateOf(false) }
    var file by remember { mutableStateOf<File?>(null) }
    var swName by remember { mutableStateOf("") }
    var filePath by remember { mutableStateOf("") }
    var pid by remember { mutableStateOf("") }
    var fileType by remember { mutableStateOf("") }
    val allUsbDevices = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val openUsbDeviceList = allUsbDevices.filter {
        it.status.value == DeviceStatus.OPENED && it.connectionType != ConnectionType.USB_OEM && it.usbDevice.productId.toString() != "16386"
    } as ArrayList<DatalogicDevice>
    val selectedUsbDevice = homeViewModel.selectedDevice.observeAsState(null).value
    DisposableEffect(Unit) {
        val target = selectedUsbDevice?.takeIf {
            it.connectionType != ConnectionType.USB_OEM || openUsbDeviceList.isEmpty()
        } ?: openUsbDeviceList.firstOrNull()
        target?.let {
            if (it != selectedUsbDevice) {
                homeViewModel.setSelectedDevice(it)
            }
        }
        onDispose {}
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                val fileName = FileUtils.getFileNameFromUri(context, it).toString()
                val fileSupport = fileName.contains(".S37", true) ||
                                  fileName.contains(".SWU", true) ||
                                  fileName.contains(".DFW", true)
                if (fileSupport)
                {
                    swName = fileName.replace(".S37", "").replace(".swu", "")
                    fileType = FileUtils.getFileNameFromUri(context, it)
                        ?.let { it1 -> FileUtils.getFileExtension(it1) }.toString().uppercase()
                    file = FileUtils.getFileFromUri(context, it)
                    isLoadFile.value = true
                    homeViewModel.loadFirmwareFile(file, fileType, context,
                        onCompleteLoadFirmware =  {
                        pid = if (fileType != FileConstants.DFW_FILE_TYPE) {
                            homeViewModel.getPid(file, fileType, context).toString()
                        } else {
                            homeViewModel.getPidDWF(file, fileType)
                        }
                    })

                    filePath = FileUtils.getDisplayPath(context, uri) ?: ""
                } else {
                    Toast.makeText(context, "This file is not supported", Toast.LENGTH_LONG).show()
                }
                filePath = FileUtils.getDisplayPath(context, uri) ?: ""
            }
        }
    )

    LaunchedEffect(Unit) {
        homeViewModel._isBulkTransferSupported.value = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UsbBTDeviceDropdown(
                modifier = Modifier
                    .semantics { contentDescription = "device_dropdown" }
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen._55sdp)),
                usbDevices = openUsbDeviceList,
                bluetoothDevices = null,
                onUsbDeviceSelected = { device ->
                    homeViewModel.setSelectedDevice(device)
                },
                selectedBluetoothDevice = null,
                selectedUsbDevice = selectedUsbDevice,
                onBluetoothDeviceSelected = { device ->
                    homeViewModel.setSelectedBluetoothDevice(device)
                }
            )
            // FW information
            if (isLoadFile.value) {
                ReleaseInformationCard(swName, pid, filePath)
                Spacer(modifier = Modifier.height(4.dp))
                UpgradeConfigurationCard(
                    checkPidEnabled = isCheckPidToggle,
                    bulkTransferEnabled = isBulkTransferToggle,
                    onCheckPidToggle = {
                        isCheckPidToggle = it
                    },
                    onBulkTransferToggle = {
                        isBulkTransferToggle = it
                        if (!isBulkTransferToggle)
                            homeViewModel._isBulkTransferSupported.postValue(false)
                    },
                    isFRS = homeViewModel.isFRS(),
                    isS37 = (fileType.uppercase() == FileConstants.S37_FILE_TYPE),
                    isDFW = fileType.uppercase() == FileConstants.DFW_FILE_TYPE
                )
            }
            // Progress Section

            Spacer(modifier = Modifier.weight(1f))

            // Buttons at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // <-- Ensures children align to tallest one
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        filePickerLauncher.launch("application/octet-stream")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    ),
                ) {
                    Text(
                        stringResource(R.string.btn_load_file),
                        color = colorResource(R.color.white),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // better than weight for spacing

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(), // <-- makes this button match height
                    onClick = {
                        try {
                            file?.let {
                                when (fileType) {
                                    FileConstants.S37_FILE_TYPE -> {
                                        homeViewModel.setPid(it, fileType, { isValid ->
                                            if (isCheckPidToggle) {
                                                if (isValid) {
                                                    handleBulkTransferAndUpgrade(
                                                        it,
                                                        isBulkTransferToggle,
                                                        homeViewModel,
                                                        context,
                                                        fileType
                                                    )
                                                } else {
                                                    homeViewModel.errorMessageUpgradeFw =
                                                        context.getString(R.string.pid_is_not_valid)
                                                    homeViewModel.showErrorMessageUpgradeFw = true
                                                }
                                            } else {
                                                handleBulkTransferAndUpgrade(
                                                    it,
                                                    isBulkTransferToggle,
                                                    homeViewModel,
                                                    context,
                                                    fileType
                                                )
                                            }
                                        }, context)
                                    }

                                    FileConstants.DFW_FILE_TYPE -> {
                                        homeViewModel.setPidDWF(it, fileType) { isValid ->
                                            if (isCheckPidToggle) {
                                                if (isValid) {
                                                    handleBulkTransferAndUpgrade(
                                                        it,
                                                        isBulkTransferToggle,
                                                        homeViewModel,
                                                        context,
                                                        fileType
                                                    )
                                                } else {
                                                    homeViewModel.errorMessageUpgradeFw =
                                                        context.getString(R.string.pid_is_not_valid)
                                                    homeViewModel.showErrorMessageUpgradeFw = true
                                                }
                                            } else {
                                                handleBulkTransferAndUpgrade(
                                                    it,
                                                    isBulkTransferToggle,
                                                    homeViewModel,
                                                    context,
                                                    fileType
                                                )
                                            }
                                        }
                                    }
                                    FileConstants.SWU_FILE_TYPE -> {
                                        handleBulkTransferAndUpgrade(
                                            it,
                                            isBulkTransferToggle,
                                            homeViewModel,
                                            context,
                                            fileType
                                        )
                                    }
                                    else -> {
                                        homeViewModel.errorMessageUpgradeFw = "This file is not supported. Please select another firmware file."
                                        homeViewModel.showErrorMessageUpgradeFw = true
                                    }
                                }
                            }
                        } catch (_: Exception){
                            Toast.makeText(
                                context,
                                context.getString(R.string.common_error_message),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    enabled = isLoadFile.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.btn_upgrade_firmware),
                        color = colorResource(R.color.white),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (homeViewModel.showErrorMessageUpgradeFw) {
                    AlertDialog(
                        onDismissRequest = {homeViewModel.dismissUpgradeFwErrorDialog()},
                        confirmButton = {
                            TextButton(onClick = {
                                homeViewModel.dismissUpgradeFwErrorDialog()
                            }) {
                                Text(stringResource(id = R.string.ok))
                            }
                        },
                        title = {
                            Text(
                                text = "Upgrade Firmware Unsuccessfully",
                                style = MaterialTheme.typography.headlineLarge
                            )
                        },
                        text = {
                            Text(
                                text = homeViewModel.errorMessageUpgradeFw,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        modifier = Modifier.padding(dimensionResource(id = R.dimen._16sdp)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen._16sdp)),
                        containerColor = Color.White,
                        properties = DialogProperties(
                            dismissOnBackPress = true,
                            dismissOnClickOutside = false
                        )
                    )
                }
            }
        }
    }
}

fun handleBulkTransferAndUpgrade(
    file: File,
    isBulkTransferToggle: Boolean,
    homeViewModel: HomeViewModel,
    context: Context,
    fileType: String
) {
    if (isBulkTransferToggle) {
        homeViewModel.getBulkTransferSupported({ supported ->
            if (isBulkTransferToggle != supported) {
                homeViewModel.errorMessageUpgradeFw = context.getString(R.string.bulk_transfer_is_not_valid)
                homeViewModel.showErrorMessageUpgradeFw = true
                return@getBulkTransferSupported
            } else {
                homeViewModel.upgradeFirmware(true)
                return@getBulkTransferSupported
            }
        })
    } else {
        if (fileType == FileConstants.SWU_FILE_TYPE) {
            if (!homeViewModel.isSWUValid(file, context)!!) {
                homeViewModel.errorMessageUpgradeFw = context.getString(R.string.swu_is_not_valid)
                homeViewModel.showErrorMessageUpgradeFw = true
                return
            }
        }
        homeViewModel.upgradeFirmware(false)
    }
}