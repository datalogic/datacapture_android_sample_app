package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.DialogProperties
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

var popup by mutableStateOf(false)
var mExpanded by mutableStateOf(false)
var mCurrentDevice by mutableStateOf<DatalogicDevice?>(null)
var mSelectedDevice by mutableStateOf<DatalogicDevice?>(null)
var mTextFieldSize by mutableStateOf(Size.Zero)

@Composable
fun DeviceDropdown(
    modifier: Modifier,
    mDevices: ArrayList<DatalogicDevice>,
    onDeviceSelected: (DatalogicDevice?) -> Unit,
    deviceStatus: DeviceStatus,
    selectedDevice: DatalogicDevice?
) {
    LaunchedEffect(mDevices) {
        mCurrentDevice = selectedDevice
        if (mCurrentDevice == null || !mDevices.contains(mCurrentDevice)) {
            mCurrentDevice = if (mDevices.isNotEmpty()) mDevices.first() else null
            onDeviceSelected(mCurrentDevice)
        }
    }

    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen._5sdp)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen._10sdp)),
        colors = CardDefaults.cardColors(Color.White),
        modifier = modifier
    ) {
        TextField(
            value = selectedDevice?.displayName ?: stringResource(id = R.string.no_devices_connected),
            textStyle = MaterialTheme.typography.labelLarge,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    mTextFieldSize = coordinates.size.toSize()
                }
                .clickable { mExpanded = !mExpanded },
            trailingIcon = {
                if (mDevices.isNotEmpty()) {
                    Icon(icon, "arrow_dropdown", tint = Color.Black)
                } else {
                    Icon(Icons.Filled.ArrowDropDown, "arrow_dropdown", Modifier.alpha(0.7f))
                }
            },
            enabled = false,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        if (mDevices.isNotEmpty()) {
            DropdownMenu(
                expanded = mExpanded,
                onDismissRequest = { mExpanded = false },
                modifier = Modifier
                    .background(color = Color.White)
                    .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
            ) {
                mDevices.forEach { device ->
                    if (device != mCurrentDevice) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = device.displayName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            onClick = {

                                if (deviceStatus != DeviceStatus.CLOSED) {
                                    mSelectedDevice = device
                                    popup = true
                                } else {
                                    mCurrentDevice = device
                                    mExpanded = false
                                    onDeviceSelected(device)
                                }
                            }
                        )
                        if (popup){
                            AlertDialogComponent(onDeviceSelected, mCurrentDevice)
                        }
                    }
                }

                if (mDevices.size == 1) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.no_other_devices)) },
                        onClick = {
                            mExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AlertDialogComponent( onDeviceSelected: (DatalogicDevice?) -> Unit, selectedDevice : DatalogicDevice?) {
    val homeViewModel = LocalHomeViewModel.current
    AlertDialog(
        onDismissRequest = { popup = false },
        confirmButton = {
            TextButton(
                onClick = {
                    popup = false
                    mExpanded = false
                    if(homeViewModel.setDropdownSelectedDevice(selectedDevice)) {
                        mCurrentDevice = mSelectedDevice
                        mExpanded = false
                        onDeviceSelected(mCurrentDevice)
                    }
                }) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { popup = false }) {
                Text(stringResource(id = R.string.dismiss))
            }
        },
        title = { Text(text = stringResource(id = R.string.alert), style = MaterialTheme.typography.headlineLarge) },
        text = { Text(text = stringResource(id = R.string.alert_message_for_device_change), style = MaterialTheme.typography.labelLarge) },
        modifier = Modifier.padding(dimensionResource(id = R.dimen._16sdp)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen._16sdp)),
        containerColor = Color.White,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    )
}