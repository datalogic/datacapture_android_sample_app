package com.datalogic.aladdin.aladdinusbapp.views.activity.customConfigurationScreen

import DatalogicBluetoothDevice
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ResetDeviceAlertDialog
import com.datalogic.aladdin.aladdinusbapp.views.compose.UsbBTDeviceDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import java.util.Locale

@Preview
@Composable
fun CustomConfigurationPortrait() {
    val homeViewModel = LocalHomeViewModel.current
    val configData = homeViewModel.customConfiguration.observeAsState().value ?: ""
    val textState = remember { mutableStateOf(configData) } // Initialize with configData
    val context = LocalContext.current
    val showDialog = remember { mutableStateOf(false) }
    val fileName = remember { mutableStateOf("") }
    val configName = homeViewModel.configName.observeAsState("").value
    val customerName = homeViewModel.customerName.observeAsState("").value
    val errorMessage = homeViewModel.msgConfigError.observeAsState("").value
    val allUsbDevices = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val openUsbDeviceList = allUsbDevices.filter {
        it.status.value == DeviceStatus.OPENED && it.connectionType != ConnectionType.USB_OEM && it.usbDevice.productId.toString() != "16386"
    } as ArrayList<DatalogicDevice>
    val selectedUsbDevice = homeViewModel.selectedDevice.observeAsState(null).value
    DisposableEffect(Unit) {
        var useCurrentSelected = false
        if (selectedUsbDevice != null && selectedUsbDevice.connectionType != ConnectionType.USB_OEM) {
            for (device in openUsbDeviceList) {
                if (device.usbDevice.deviceName == selectedUsbDevice.usbDevice.deviceName) {
                    homeViewModel.setSelectedDevice(device)
                    useCurrentSelected = true
                    break
                }
            }
        }
        if (!useCurrentSelected) {
            homeViewModel.setSelectedDevice(openUsbDeviceList.firstOrNull())
        }
        homeViewModel.getDeviceConfigName()
        onDispose {}
    }

    // Configuration result dialog state
    val showConfigResultDialog = remember { mutableStateOf(false) }
    val configResultTitle = remember { mutableStateOf("") }
    val configResultMessage = remember { mutableStateOf("") }
    // Set up callback for configuration results
    LaunchedEffect(Unit) {
        homeViewModel.setConfigurationResultCallback(object :
            HomeViewModel.ConfigurationResultCallback {
            override fun onConfigurationResult(isSuccess: Boolean, title: String, message: String) {
                configResultTitle.value = title
                configResultMessage.value = message
                showConfigResultDialog.value = true
            }
        })
    }

    // If the ViewModel's data changes (e.g., after a "Read" operation), update the local textState
    LaunchedEffect(configData) {
        if (textState.value != configData) { // Avoid unnecessary updates
            textState.value = configData
        }
    }

    // DisposableEffect to clear state when composable disappears
    DisposableEffect(Unit) {
        onDispose {
            homeViewModel.clearConfig()
        }
    }

    // ActivityResultLauncher for picking a text file
    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Read the content of the selected file
            context.contentResolver.openInputStream(it)?.use { inputStream ->
                val fileContent = inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
                //textState.value = fileContent // Update the TextField with file content
                // Optionally, you can also update the ViewModel here if needed
                homeViewModel.updateCustomConfiguration(fileContent)
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val scroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
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
                        homeViewModel.getDeviceConfigName()
                    },
                    selectedBluetoothDevice = null,
                    selectedUsbDevice = selectedUsbDevice,
                    onBluetoothDeviceSelected = { device ->
                        homeViewModel.setSelectedBluetoothDevice(device)
                    }
                )
                if (selectedUsbDevice != null && configName.isEmpty()) {
                    homeViewModel.getDeviceConfigName()
                }
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_customer" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._10sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = "Customer Name",
                    style = MaterialTheme.typography.headlineLarge
                )
                BasicTextField(
                    value = customerName,
                    onValueChange = { homeViewModel.updateCustomerName(it) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                        )
                        .padding(8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_config" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._10sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = "Config Name",
                    style = MaterialTheme.typography.headlineLarge
                )
                BasicTextField(
                    value = configName,
                    onValueChange = {
                        if (it.length <= 10)
                            homeViewModel.updateCurrentConfigName(it)
                    },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                        )
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp)) // Push buttons to bottom

            LineNumberedTextField(
                text = configData,
                onTextChange = { textState.value = it },
                modifier = Modifier
//                    .weight(1f)
                    .height(dimensionResource(id = R.dimen._190sdp))
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // Push buttons to bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_read" }
                        .weight(0.5f)
                        .padding(horizontal = dimensionResource(id = R.dimen._16sdp))
                        .wrapContentSize(),
                    onClick = {
                        homeViewModel.readCustomConfig()
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary)),
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_read),
                        color = Color.White
                    )
                }

                Button(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_write" }
                        .weight(0.5f)
                        .padding(horizontal = dimensionResource(id = R.dimen._16sdp))
                        .wrapContentSize(),
                    onClick = { homeViewModel.writeCustomConfig(configurationData = textState.value) },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary)),
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_write),
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_load" }
                        .weight(0.5f)
                        .padding(horizontal = dimensionResource(id = R.dimen._16sdp))
                        .wrapContentSize(),
                    onClick = {
                        pickFileLauncher.launch("text/*")
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary)),
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_load),
                        color = Color.White
                    )
                }

                Button(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_save" }
                        .weight(0.5f)
                        .padding(horizontal = dimensionResource(id = R.dimen._16sdp))
                        .wrapContentSize(),
                    onClick = { showDialog.value = true },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary)),
                ) {
                    Text(
                        text = stringResource(id = R.string.btn_save),
                        color = Color.White
                    )
                }
            }
        }
    }
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Enter File Name") },
            text = {
                TextField(
                    value = fileName.value,
                    onValueChange = { fileName.value = it },
                    label = { Text("File Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                        homeViewModel.saveConfigData(fileName.value)
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog.value = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (errorMessage != "") {
        AlertDialog(
            onDismissRequest = { homeViewModel.setMsgConfigError("") },
            title = { Text("Error: $errorMessage") },
            confirmButton = {
                Button(
                    onClick = {
                        homeViewModel.setMsgConfigError("")
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Configuration result dialog
    if (showConfigResultDialog.value) {
        Log.d(
            "Custom config",
            "[showConfigResultDialog] configResultTitle: ${configResultTitle.value}"
        )

        if (configResultTitle.value.toUpperCase(Locale.ROOT) == "SUCCESSFULLY") {
            homeViewModel.showResetDeviceDialog = true
            Log.d("Custom config", "[showConfigResultDialog] showResetDeviceDialog == true")
        } else {
            AlertDialog(
                onDismissRequest = { showConfigResultDialog.value = false },
                title = { Text(configResultTitle.value) },
                text = { Text(configResultMessage.value) },
                confirmButton = {
                    Button(
                        onClick = { showConfigResultDialog.value = false }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                }
            )
        }
    }

    if (homeViewModel.showResetDeviceDialog) {
        Log.d("Custom config", "[showConfigResultDialog] showResetDeviceDialog == true")
        ResetDeviceAlertDialog(homeViewModel, customConfig = true)
        showConfigResultDialog.value = false
        Log.d("Custom config", "[showConfigResultDialog] showConfigResultDialog.value = false")
    }
}