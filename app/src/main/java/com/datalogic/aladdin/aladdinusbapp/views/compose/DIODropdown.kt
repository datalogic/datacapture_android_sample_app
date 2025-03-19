package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.toSize
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbscannersdk.model.UsbDeviceDescriptor
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DIOCmdValue
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun DIODropdown(modifier: Modifier, selectedCommand: DIOCmdValue, onCommandSelected: (DIOCmdValue) -> Unit,
                selectedDevice : (UsbDeviceDescriptor?), onDeviceSelected: (UsbDeviceDescriptor?) -> Unit, mDevices: ArrayList<UsbDeviceDescriptor>, status: DeviceStatus?){

    var mExpanded by remember { mutableStateOf(false) }
    val commands = DIOCmdValue.entries.map { it }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    var mCurrentCommand by remember {mutableStateOf("")}

    LaunchedEffect(mDevices, selectedCommand) {
        mCurrentCommand = if (mDevices.isNotEmpty()) selectedCommand.value else "No devices connected"
        var deviceName = selectedDevice
        if (selectedDevice == null || !mDevices.contains(selectedDevice)) {
            deviceName = if (mDevices.isNotEmpty()) mDevices.first() else null
        }
        onDeviceSelected(deviceName)
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
            value = mCurrentCommand,
            textStyle = MaterialTheme.typography.labelLarge,
            onValueChange = {},
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    mTextFieldSize = coordinates.size.toSize()
                }
                .clickable { mExpanded = !mExpanded },
            trailingIcon = {
                if (mDevices.isNotEmpty() && status == DeviceStatus.OPENED) {
                    Icon(icon, stringResource(id = R.string.arrow_dropdown), tint = Color.Black)
                } else {
                    Icon(Icons.Filled.KeyboardArrowDown, stringResource(id = R.string.arrow_dropdown))
                } },
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
        if (mDevices.isNotEmpty() && status == DeviceStatus.OPENED) {
            DropdownMenu(
                expanded = mExpanded,
                onDismissRequest = { mExpanded = false },
                modifier = Modifier
                    .background(color = Color.White)
                    .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
            ) {
                commands.forEach { command ->
                    if (command.value != mCurrentCommand) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = command.value,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            onClick = {
                                onCommandSelected(command)
                                mExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}