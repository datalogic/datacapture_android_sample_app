package com.datalogic.aladdin.aladdinusbapp.views.activity.devicesScreen

// SPDX-License-Identifier: MIT
// File: DevicesScreen.kt
// A clean, Compose Material 3 UI to view "Active" vs "Inactive" devices and toggle status.
// Drop this into your app module and wire the screen in your NavHost or Activity.


// SPDX-License-Identifier: MIT
// File: DevicesScreen.kt
// A Compose Material 3 UI to view and manage devices with Active/Inactive status.
// NEW: Multi-select with bulk Activate/Deactivate, long-press to enter selection mode,
// per-item selection checkboxes, and quick actions in the TopAppBar.

// SPDX-License-Identifier: MIT
// File: DevicesScreen.kt
// A Compose Material 3 UI to view and manage devices with Active/Inactive status.
// NEW: Multi-select with bulk Activate/Deactivate, long-press to enter selection mode,
// per-item selection checkboxes, and quick actions in the TopAppBar.


import DatalogicBluetoothDevice
import android.app.Activity
import android.content.ContentValues.TAG
import android.hardware.usb.UsbDevice
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButtonRow
import com.datalogic.aladdin.aladdinusbapp.views.compose.ConnectionTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceTypeDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.livedata.observeAsState

// --------- UI layer ---------

@Composable
fun BluetoothDeviceItem(device: DatalogicBluetoothDevice, modifier: Modifier = Modifier) {
    BluetoothDeviceRow(device = device, modifier = modifier)
}

@Composable
fun DeviceRow(
    dlDevice: DatalogicDevice?,
    usbDevice: UsbDevice?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val homeViewModel = LocalHomeViewModel.current
    var isManual by remember { mutableStateOf(false) }
    val icon = if (isManual)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    val autoDetectChecked by homeViewModel.autoDetectChecked.observeAsState(true)
    val isOpen: Boolean = dlDevice?.status?.value == DeviceStatus.OPENED

    val buttonText = if (isOpen) {
        stringResource(id = R.string.close)
    } else {
        stringResource(id = R.string.open)
    }

    val usbDeviceName = "${usbDevice?.productName}-${usbDevice?.vendorId}-${usbDevice?.productId}"
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            if (isOpen) Color(0xFF34C759) else Color(
                                0xFF999999
                            )
                        )
                )
                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = dlDevice?.displayName ?: usbDeviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = stringResource(R.string.usb_device),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(12.dp))
                if (!autoDetectChecked) {
                    IconButton(onClick = {
                        isManual = !isManual
                    }) {
                        Icon(
                            imageVector = icon,
                            contentDescription = stringResource(id = R.string.arrow_dropdown),
                            tint = Color.Black
                        )
                    }
                } else {
                    CustomButtonRow(
                        modifier = Modifier
                            .weight(0.5f)
                            .semantics { contentDescription = "btn_open" },
                        openState = isOpen,
                        name = buttonText,
                        onClick = {
                            activity?.let {
                                if (isOpen) {
                                    // If you keep a raw row “open” concept, close by usb id
                                    homeViewModel.closeUsbDevice(dlDevice)
                                } else {
                                    homeViewModel.openUsbDevice(dlDevice)
                                    // After open, render it via the DatalogicDevice list row
                                }
                            }
                        }
                    )
                }
            }
            if(isManual){
                Column(
                    modifier = Modifier
                        .semantics { contentDescription = "device_settings" }
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(id = R.dimen._5sdp))
                ) {
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "lbl_device_settings" }
                            .fillMaxWidth()
                            .padding(
                                start = dimensionResource(id = R.dimen._10sdp),
                                bottom = dimensionResource(id = R.dimen._5sdp)
                            ),
                        text = stringResource(id = R.string.device_settings),
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
                                .semantics { contentDescription = "lbl_device_vid" }
                                .fillMaxWidth()
                                .padding(
                                    top = dimensionResource(id = R.dimen._10sdp),
                                    bottom = dimensionResource(id = R.dimen._5sdp)
                                ),
                            text = "VID: " + (usbDevice?.vendorId ?: "None"),
                            style = MaterialTheme.typography.labelLarge
                        )

                        Text(
                            modifier = Modifier
                                .semantics { contentDescription = "lbl_device_pid" }
                                .fillMaxWidth()
                                .padding(
                                    top = dimensionResource(id = R.dimen._10sdp),
                                    bottom = dimensionResource(id = R.dimen._5sdp)
                                ),
                            text = "PID: " + (usbDevice?.productId ?: "None"),
                            style = MaterialTheme.typography.labelLarge
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._5sdp)))

                        DeviceTypeDropdown(
                            modifier = Modifier
                                .semantics { contentDescription = "device_type_dropdown" }
                                .fillMaxWidth()
                                .height(dimensionResource(id = R.dimen._55sdp)),
                            homeViewModel.getSelectedDeviceType(),
                            onDeviceTypeSelected = {
                                homeViewModel.setSelectedDeviceType(it)
                            }
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))

                        ConnectionTypeDropdown(
                            modifier = Modifier
                                .semantics { contentDescription = "connection_type_dropdown" }
                                .fillMaxWidth()
                                .height(dimensionResource(id = R.dimen._55sdp)),
                            homeViewModel.getSelectedConnectionType(),
                            onDeviceTypeSelected = {
                                homeViewModel.setSelectedConnectionType(it)
                            }
                        )

                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))

                    }
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._5sdp)))

                    CustomButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "btn_open" },
                        openState = isOpen,
                        name = buttonText,
                        onClick = {
                            if (usbDevice != null) homeViewModel.setSelectedUsbDevice(usbDevice)

                            if (isOpen) {
                                // Close the actual instance for this row
                                dlDevice?.let { homeViewModel.closeUsbDevice(it) }
                            } else {
                                // Open (VM will create/reuse the DatalogicDevice for this USB and add to deviceList)
                                homeViewModel.setSelectedUsbDevice(usbDevice)
                                homeViewModel.openUsbDevice(dlDevice)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BluetoothDeviceRow(
    device: DatalogicBluetoothDevice,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val homeViewModel = LocalHomeViewModel.current
    val isOpen = device.status.value == DeviceStatus.OPENED
    val buttonText = if (isOpen) {
        stringResource(id = R.string.close)
    } else {
        stringResource(id = R.string.open)
    }
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.status.value == DeviceStatus.OPENED) Color(0xFF34C759) else Color(
                            0xFF999999
                        )
                    )
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.bluetooth_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))
            CustomButtonRow(
                modifier = Modifier
                    .weight(0.5f)
                    .semantics { contentDescription = "btn_open" },
                openState = isOpen,
                name = buttonText,
                onClick = {
                    activity?.let {
                        if(isOpen) {
                            Log.d(TAG, "btn_close on click")
                            homeViewModel.closeBluetoothDevice(device)
                        } else {
                            Log.d(TAG, "btn_open on click")
                            homeViewModel.coroutineOpenBluetoothDevice(device, activity)
                        }
                    }
                }
            )
        }
    }
}



