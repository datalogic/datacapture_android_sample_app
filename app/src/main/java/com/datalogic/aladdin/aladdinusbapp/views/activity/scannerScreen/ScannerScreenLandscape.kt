package com.datalogic.aladdin.aladdinusbapp.views.activity.scannerScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel.ScanUi
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.LabelCodeTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.LabelIDControlDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

/**
 * ScannerScreenPortrait – multi-section version
 *
 * This replaces the single-device layout with one section per OPEN device (USB + Bluetooth),
 * while keeping your original look/feel (label/data/raw fields). Each section is fed by a
 * per-device scan flow provided by the HomeViewModel helpers discussed earlier.
 */

// --- UI model for a scan event (matches helpers in HomeViewModel) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreenLandscape(modifier: Modifier = Modifier) {
    val homeViewModel = LocalHomeViewModel.current

    // Observe current device lists
    val usbDevices = homeViewModel.deviceList.observeAsState(arrayListOf()).value
    val btDevices = homeViewModel.allBluetoothDevices.observeAsState(arrayListOf()).value

    // Filter to OPEN devices
    val openUsb =
        remember(usbDevices) { usbDevices.filter { it.status.value == DeviceStatus.OPENED } }
    val openBt = remember(btDevices) { btDevices.filter { it.status.value == DeviceStatus.OPENED } }

    // Compose a single list for rendering
    data class UiDevice(val id: String, val name: String, val isUsb: Boolean)

    val devices = remember(openUsb, openBt) {
        val usb = openUsb.map {
            UiDevice(
                id = it.usbDevice.deviceId.toString(),
                name = it.displayName,
                isUsb = true
            )
        }
        val bt = openBt.map {
            UiDevice(
                id = it.bluetoothDevice.address,
                name = it.name ?: it.bluetoothDevice.address,
                isUsb = false
            )
        }
        usb + bt
    }
    if (devices.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No OPEN devices detected")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices, key = { it.id }) { dev ->
                val scan: ScanUi by homeViewModel
                    .scanFlowFor(dev.id)
                    .collectAsStateWithLifecycle(initialValue = ScanUi())
                ScannerDeviceSection(
                    title = if (dev.isUsb) stringResource(R.string.usb_device)
                    else stringResource(R.string.bluetooth_device),
                    deviceName = dev.name,
                    scan = scan,
                    homeViewModel = homeViewModel,
                    deviceId = dev.id
                )
            }
        }
    }
}

@Composable
private fun ScannerDeviceSection(
    title: String,
    deviceName: String,
    scan: ScanUi,
    homeViewModel: HomeViewModel,
    deviceId: String
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = deviceName, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = title, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Keep the original look/feel: Label / Data / Raw boxes
            Column(
                modifier = Modifier
                    .semantics { contentDescription = "scanner_data" }
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen._5sdp))
            ) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_scanner_data" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._5sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = stringResource(id = R.string.scanner_data),
                    style = MaterialTheme.typography.headlineLarge
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                        )
                        .padding(horizontal = dimensionResource(id = R.dimen._16sdp)),
                ) {
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "lbl_scan_raw_data" }
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen._15sdp),
                                bottom = dimensionResource(id = R.dimen._5sdp)
                            ),
                        text = stringResource(id = R.string.scan_raw_data),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "scan_raw_data" }
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen._81sdp))
                            .border(
                                BorderStroke(1.dp, Color.Black),
                                RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                            )
                            .padding(dimensionResource(id = R.dimen._8sdp))
                            .verticalScroll(rememberScrollState()),
                        text = scan.raw
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "lbl_scan_data" }
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen._15sdp),
                                bottom = dimensionResource(id = R.dimen._5sdp)
                            ),
                        text = stringResource(id = R.string.scan_data),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "scan_data" }
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen._36sdp))
                            .border(
                                BorderStroke(1.dp, Color.Black),
                                RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                            )
                            .padding(dimensionResource(id = R.dimen._8sdp))
                            .verticalScroll(rememberScrollState()),
                        text = scan.data
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "lbl_scan_data_label" }
                            .fillMaxWidth()
                            .padding(vertical = dimensionResource(id = R.dimen._5sdp)),
                        text = stringResource(id = R.string.scan_data_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "scanned_data_label" }
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen._35sdp))
                            .border(
                                BorderStroke(1.dp, Color.Black),
                                RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                            )
                            .padding(dimensionResource(id = R.dimen._8sdp)),
                        text = scan.label
                    )
                    Button(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_clear_fields" }
                            .padding(vertical = dimensionResource(id = R.dimen._16sdp))
                            .wrapContentSize(),
                        onClick = {
                            homeViewModel.perDeviceClear(deviceId = deviceId)
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text(
                            text = stringResource(id = R.string.clear_fields),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))

                    LabelIDControlDropdown(
                        modifier = Modifier
                            .semantics { contentDescription = "label_id_control_dropdown" }
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen._55sdp)),
                        homeViewModel.selectedLabelIDControl.observeAsState().value,
                        onLabelIDControlSelected = {
                            homeViewModel.setSelectedLabelIDControl(it, deviceId)
                        }
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))

                    LabelCodeTypeDropdown(
                        modifier = Modifier
                            .semantics { contentDescription = "label_code_type_dropdown" }
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen._55sdp)),
                        homeViewModel.selectedLabelCodeType.observeAsState().value,
                        onLabelCodeTypeSelected = {
                            homeViewModel.setSelectedLabelCodeType(it, deviceId)
                        }
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))

                }
            }
        }
    }
}