package com.datalogic.aladdin.aladdinusbapp.views.activity.scaleScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomTextField
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel.ScaleUi

@Composable
fun ScaleScreenPortrait(modifier: Modifier = Modifier) {
    val homeViewModel = LocalHomeViewModel.current

    // Observe current device lists
    val usbDevices = homeViewModel.deviceList.observeAsState(arrayListOf()).value

    // Filter to OPEN devices
    val isScaleDevice =
        remember(usbDevices) { usbDevices.filter { it.status.value == DeviceStatus.OPENED && it.isScaleAvailable() } }

    // Compose a single list for rendering
    data class UiDevice(val id: String, val name: String, val isUsb: Boolean)

    val devices = remember(isScaleDevice) {
        val usb = isScaleDevice.map {
            UiDevice(
                id = it.usbDevice.deviceId.toString(),
                name = it.displayName,
                isUsb = true
            )
        }
        usb
    }
    if (devices.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No scale devices detected")
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices, key = { it.id }) { dev ->
                // Per-device Scale
                val scale = homeViewModel
                    .scaleFlowFor(dev.id)
                    .collectAsStateWithLifecycle(initialValue = ScaleUi())
                    .value

                ScaleDeviceSection(
                    title = stringResource(R.string.usb_device),
                    deviceName = dev.name,
                    scale = scale,                           // <-- pass ScaleUi
                    homeViewModel = homeViewModel,
                    deviceId = dev.id
                )
            }
        }
    }
}

@Composable
fun ScaleDeviceSection(
    title: String,
    deviceName: String,
    scale: HomeViewModel.ScaleUi, // <-- instead of reading global LiveData
    homeViewModel: HomeViewModel,
    deviceId: String
) {
    val scale by homeViewModel.scaleFlowFor(deviceId).collectAsStateWithLifecycle(ScaleUi())
    val enabled by homeViewModel.scaleEnabledFlowFor(deviceId).collectAsStateWithLifecycle(false)
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .semantics { contentDescription = "scale_data" }
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen._10sdp))
        ) {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_scale_data" }
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen._10sdp),
                        bottom = dimensionResource(id = R.dimen._5sdp)
                    ),
                text = "Scale - $deviceName",
                style = MaterialTheme.typography.headlineLarge
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Color.Black),
                        RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                    )
                    .padding(
                        horizontal = dimensionResource(id = R.dimen._16sdp),
                        vertical = dimensionResource(id = R.dimen._10sdp)
                    ),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen._8sdp))
            ) {
                CustomTextField(
                    textValue = scale.status,
                    onValueChange = { },
                    readOnly = true,
                    labelText = "Scale Status",
                    enabledStatus = true
                )
                CustomTextField(
                    textValue = scale.weight,
                    onValueChange = { },
                    readOnly = true,
                    labelText = "Weight",
                    enabledStatus = true
                )
                CustomTextField(
                    textValue = scale.unit.toString(),
                    onValueChange = { },
                    readOnly = true,
                    labelText = "Unit",
                    enabledStatus = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CustomButton(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_enable_scale" }
                            .weight(0.5f),
                        buttonState = !enabled,
                        stringResource(id = R.string.start),
                        onClick = {
                            // Start scale for THIS device
                            homeViewModel.selectedDevice.value?.let {
                                if (it.usbDevice.deviceId.toString() == deviceId) {
                                    homeViewModel.startScaleHandler(deviceId)
                                } else {
                                    // optionally switch selection to this device then start:
                                    homeViewModel.setSelectedDevice(
                                        homeViewModel.deviceList.value?.firstOrNull { d ->
                                            d.usbDevice.deviceId.toString() == deviceId
                                        }
                                    )
                                    homeViewModel.startScaleHandler(deviceId)
                                }
                            } ?: run {
                                homeViewModel.setSelectedDevice(
                                    homeViewModel.deviceList.value?.firstOrNull { d ->
                                        d.usbDevice.deviceId.toString() == deviceId
                                    }
                                )
                                homeViewModel.startScaleHandler(deviceId)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._5sdp)))
                    CustomButton(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_disable_scale" }
                            .weight(0.5f),
                        buttonState = enabled,
                        stringResource(id = R.string.stop),
                        onClick = { homeViewModel.stopScaleHandler(deviceId) }
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._5sdp)))
                    CustomButton(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_clear_scale_data" }
                            .weight(0.5f),
                        buttonState = true,
                        stringResource(id = R.string.clear),
                        onClick = { homeViewModel.perDeviceScaleClear(deviceId) } // <-- clear THIS device only
                    )
                }
            }
        }
    }
}
