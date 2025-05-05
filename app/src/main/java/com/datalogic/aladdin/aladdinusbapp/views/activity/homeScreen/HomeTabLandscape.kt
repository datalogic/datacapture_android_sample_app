package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils
import com.datalogic.aladdin.aladdinusbapp.views.compose.ConnectionTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.UsbDeviceDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun HomeTabLandscape() {
    val homeViewModel = LocalHomeViewModel.current

    val deviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val scanLabel = homeViewModel.scanLabel.observeAsState("").value
    val scanData = homeViewModel.scanData.observeAsState("").value
    val status = homeViewModel.status.observeAsState(DeviceStatus.NONE).value
    val selectedDevice = homeViewModel.selectedDevice.observeAsState(null).value

    var autoDetectChecked = homeViewModel.autoDetectChecked.observeAsState(true).value
    val usbDeviceList = homeViewModel.usbDeviceList.observeAsState(ArrayList()).value
    val selectedUsbDevice = homeViewModel.selectedUsbDevice.observeAsState(null).value

    Column(
        modifier = Modifier
            .semantics { contentDescription = "home_tab_content_layout" }
            .padding(
                start = dimensionResource(id = R.dimen._35sdp),
                top = dimensionResource(id = R.dimen._20sdp),
            )
    ) {
        if (autoDetectChecked) {
            DeviceDropdown(
                modifier = Modifier
                    .semantics { contentDescription = "device_list_dropdown" }
                    .padding(bottom = dimensionResource(id = R.dimen._20sdp))
                    .fillMaxWidth()
                    .wrapContentHeight(),
                deviceList,
                onDeviceSelected = {
                    homeViewModel.setSelectedDevice(it)
                },
                status,
                selectedDevice
            )
        } else {
            UsbDeviceDropdown(
                modifier = Modifier
                    .semantics { contentDescription = "usb_device_list_dropdown" }
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen._55sdp)),
                selectedUsbDevice,
                usbDeviceList,
                onUsbDeviceSelected = {
                    homeViewModel.setSelectedUsbDevice(it)
                }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = dimensionResource(id = R.dimen._15sdp),
                    bottom = dimensionResource(id = R.dimen._5sdp)
                ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Switch(
                checked = autoDetectChecked,
                onCheckedChange = {
                    homeViewModel.setAutoDetectChecked(it)
                }
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._15sdp)))

            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_auto_detect_device" }
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen._15sdp),
                        bottom = dimensionResource(id = R.dimen._5sdp)
                    ),
                text = stringResource(id = R.string.auto_detect_device),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (!autoDetectChecked) {
            Column(
                modifier = Modifier
                    .semantics { contentDescription = "device_settings" }
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen._10sdp))
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
                        text = "VID: " + (selectedUsbDevice?.vendorId?:"None"),
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
                        text = "PID: " + (selectedUsbDevice?.productId?:"None"),
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
            }
        }

        Column(
            modifier = Modifier
                .semantics { contentDescription = "scanner_data" }
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen._10sdp))
        ) {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_scanner_data" }
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen._5sdp)),
                text = stringResource(id = R.string.scanner_data),
                style = MaterialTheme.typography.headlineLarge
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(8.dp))
                    .padding(horizontal = dimensionResource(id = R.dimen._16sdp)),
            ) {
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
                        .weight(1f)
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(5.dp))
                        .padding(dimensionResource(id = R.dimen._8sdp))
                        .verticalScroll(rememberScrollState()),
                    text = scanData
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
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(5.dp))
                        .padding(dimensionResource(id = R.dimen._8sdp)),
                    text = scanLabel
                )
                Button(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_clear_fields" }
                        .padding(vertical = dimensionResource(id = R.dimen._16sdp))
                        .wrapContentSize(),
                    onClick = {
                        homeViewModel.clearScanData()
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                ) {
                    Text(
                        text = stringResource(id = R.string.clear_fields),
                        color = Color.White
                    )
                }
            }
        }

        if (selectedDevice?.deviceType?.name == "FRS") {
            val scaleStatus by homeViewModel.scaleStatus.observeAsState("")
            val scaleWeight by homeViewModel.scaleWeight.observeAsState("")
            val scaleProtocolStatus by homeViewModel.scaleProtocolStatus.observeAsState(
                Pair(
                    false,
                    ""
                )
            )

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
                    text = "Scale",
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
                    ComposableUtils.CustomTextField(
                        textValue = scaleStatus,
                        onValueChange = { },
                        readOnly = true,
                        labelText = "Scale Status",
                        enabledStatus = true
                    )

                    ComposableUtils.CustomTextField(
                        textValue = scaleWeight,
                        onValueChange = { },
                        readOnly = true,
                        labelText = "Weight",
                        enabledStatus = true
                    )

                    Button(
                        onClick = { homeViewModel.clearScaleData() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text("Clear Scale Data", color = Color.White)
                    }

                    Button(
                        onClick = { homeViewModel.checkScaleProtocol() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = status == DeviceStatus.OPENED,
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text("Check Scale Protocol", color = Color.White)
                    }

                    Text(
                        text = if (status == DeviceStatus.OPENED) {
                            scaleProtocolStatus.second
                        } else "",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(id = R.dimen._16sdp),
                                vertical = dimensionResource(id = R.dimen._10sdp)
                            )
                    )

                    if (!scaleProtocolStatus.first && status == DeviceStatus.OPENED && scaleProtocolStatus.second !== "") {
                        Button(
                            onClick = { homeViewModel.enableScaleProtocol() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                        ) {
                            Text("Enable Scale Protocol And Reset Device", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}