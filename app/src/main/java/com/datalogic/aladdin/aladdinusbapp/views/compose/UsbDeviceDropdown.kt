package com.datalogic.aladdin.aladdinusbapp.views.compose

import android.hardware.usb.UsbDevice
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

var mCurrentUsbDevice by mutableStateOf<UsbDevice?>(null)

@Composable
fun UsbDeviceDropdown(modifier: Modifier, selectedUsbDevice: UsbDevice?, usbDevices: ArrayList<UsbDevice>, onUsbDeviceSelected: (UsbDevice?) -> Unit){
    var mExpanded by remember { mutableStateOf(false) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}

    LaunchedEffect(selectedUsbDevice, usbDevices) {
        mCurrentUsbDevice = selectedUsbDevice
        if ((mCurrentUsbDevice == null && usbDevices.isNotEmpty()) ||
            (mCurrentUsbDevice != null && usbDevices.isNotEmpty() && !usbDevices.contains(mCurrentUsbDevice))) {
            mCurrentUsbDevice = usbDevices.first()
            onUsbDeviceSelected(mCurrentUsbDevice)
        } else if (mCurrentUsbDevice != null && usbDevices.isEmpty()) {
            mCurrentUsbDevice = null
            onUsbDeviceSelected(mCurrentUsbDevice)
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
        var deviceDisplayName: String = stringResource(id = R.string.no_devices_connected)
        if (selectedUsbDevice != null) {
            deviceDisplayName = "${selectedUsbDevice.productName}-${selectedUsbDevice.vendorId}-${selectedUsbDevice.productId}"
        }
        TextField(
            value = deviceDisplayName,
            textStyle = MaterialTheme.typography.labelLarge,
            onValueChange = {},
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    mTextFieldSize = coordinates.size.toSize()
                }
                .clickable { mExpanded = !mExpanded },
            trailingIcon = {
                    Icon(icon, stringResource(id = R.string.arrow_dropdown), tint = Color.Black)
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

        if (usbDevices.isNotEmpty()) {
            DropdownMenu(
                expanded = mExpanded,
                onDismissRequest = { mExpanded = false },
                modifier = Modifier
                    .background(color = Color.White)
                    .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
            ) {
                usbDevices.forEach { usbDeviceElem ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${usbDeviceElem.productName}-${usbDeviceElem.vendorId}-${usbDeviceElem.productId}",
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        onClick = {
                            onUsbDeviceSelected(usbDeviceElem)
                            mCurrentUsbDevice = usbDeviceElem
                            mExpanded = false
                        }
                    )
                }
            }
        }
    }
}