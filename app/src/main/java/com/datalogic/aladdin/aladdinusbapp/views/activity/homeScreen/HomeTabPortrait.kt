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
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbapp.views.compose.ConnectionTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.RestartDeviceDialog
import com.datalogic.aladdin.aladdinusbapp.views.compose.UsbDeviceDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun HomeTabPortrait() {

    val homeViewModel = LocalHomeViewModel.current
    val deviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val status = homeViewModel.status.observeAsState(DeviceStatus.NONE).value

    val scanLabel = homeViewModel.scanLabel.observeAsState("").value
    val scanData = homeViewModel.scanData.observeAsState("").value
    val scanRawData = homeViewModel.scanRawData.observeAsState("").value
    val selectedDevice = homeViewModel.selectedDevice.observeAsState(null).value

    val autoDetectChecked = homeViewModel.autoDetectChecked.observeAsState(true).value
    val usbDeviceList = homeViewModel.usbDeviceList.observeAsState(ArrayList()).value
    val selectedUsbDevice = homeViewModel.selectedUsbDevice.observeAsState(null).value
    val isEnableScale = homeViewModel.isEnableScale.observeAsState(false).value
    val isScaleAvailable = homeViewModel.isScaleAvailable.observeAsState(false).value

    val content = @Composable {

        if (autoDetectChecked) {
            DeviceDropdown(
                modifier = Modifier
                    .semantics { contentDescription = "device_list_dropdown" }
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen._55sdp)),
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
            val isLoggingEnabled by homeViewModel.isLoggingEnabled.observeAsState(false)
            
            Switch(
                checked = isLoggingEnabled,
                onCheckedChange = {
                    homeViewModel.toggleLogging()
                }
            )

            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._15sdp)))

            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_logging_toggle" }
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen._15sdp),
                        bottom = dimensionResource(id = R.dimen._5sdp)
                    ),
                text = if (isLoggingEnabled) "Logging Enabled" else "Logging Disabled",
                style = MaterialTheme.typography.labelLarge
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
                        text = "VID: " + (selectedUsbDevice?.vendorId ?: "None"),
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
                        text = "PID: " + (selectedUsbDevice?.productId ?: "None"),
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
                .padding(vertical = dimensionResource(id = R.dimen._10sdp))
        ) {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_scanner_data" }
                    .fillMaxWidth()
                    .padding(
                        start = dimensionResource(id = R.dimen._10sdp),
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
                    text = scanRawData
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
                        .border(
                            BorderStroke(1.dp, Color.Black),
                            RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                        )
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

        if (isScaleAvailable) {
            val scaleStatus by homeViewModel.scaleStatus.observeAsState("")
            val scaleWeight by homeViewModel.scaleWeight.observeAsState("")
            val scaleUnit by homeViewModel.scaleUnit.observeAsState("")
            /*val scaleProtocolStatus by homeViewModel.scaleProtocolStatus.observeAsState(
                Pair(
                    false,
                    ""
                )
            )*/

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

                    ComposableUtils.CustomTextField(
                        textValue = scaleUnit.toString(),
                        onValueChange = { },
                        readOnly = true,
                        labelText = "Unit",
                        enabledStatus = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CustomButton(
                            modifier = Modifier
                                .semantics { contentDescription = "btn_enable_scale" }
                                .weight(0.5f),
                            buttonState = (!isEnableScale && status == DeviceStatus.OPENED),
                            stringResource(id = R.string.start),
                            onClick = {
                                homeViewModel.startScaleHandler()
                            }
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._5sdp)))
                        CustomButton(
                            modifier = Modifier
                                .semantics { contentDescription = "btn_disable_scale" }
                                .weight(0.5f),
                            buttonState = (isEnableScale && status == DeviceStatus.OPENED),
                            stringResource(id = R.string.stop),
                            onClick = {
                                homeViewModel.stopScaleHandler()
                            }
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._5sdp)))
                        CustomButton(
                            modifier = Modifier
                                .semantics { contentDescription = "btn_clear_scale_data" }
                                .weight(0.5f),
                            buttonState = status == DeviceStatus.OPENED,
                            stringResource(id = R.string.clear),
                            onClick = { homeViewModel.clearScaleData() }
                        )
                    }

                    /*Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CustomButton(
                            modifier = Modifier
                                .semantics {contentDescription = "btn_check_scale" }
                                .weight(0.5f),
                            buttonState = status == DeviceStatus.OPENED,
                            stringResource(id = R.string.check_scale),
                            onClick = { homeViewModel.checkScaleProtocol() }
                        )
                    }*/

                    /*if (!scaleProtocolStatus.first && status == DeviceStatus.OPENED && scaleProtocolStatus.second !== "") {
                        Button(
                            onClick = { homeViewModel.enableScaleProtocol() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                        ) {
                            Text(
                                "Enable Scale Protocol",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Text(
                        text = if (status == DeviceStatus.OPENED) {
                            scaleProtocolStatus.second
                        } else "",
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = dimensionResource(id = R.dimen._16sdp),
                                vertical = dimensionResource(id = R.dimen._10sdp)
                            )
                    )*/
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomButton(
                modifier = Modifier
                    .weight(0.5f)
                    .semantics { contentDescription = "btn_open" },
                buttonState = (status == DeviceStatus.CLOSED &&
                        (deviceList.isNotEmpty() || usbDeviceList.isNotEmpty())),
                stringResource(id = R.string.open),
                onClick = {
                    homeViewModel.openDevice()
                }
            )
            Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._10sdp)))
            CustomButton(
                modifier = Modifier
                    .weight(0.5f)
                    .semantics { contentDescription = "btn_close" },
                buttonState = (status == DeviceStatus.OPENED),
                stringResource(id = R.string.close),
                onClick = {
                    homeViewModel.closeDevice()
                }
            )
        }
        if (homeViewModel.showResetDeviceDialog) {
            RestartDeviceDialog(homeViewModel)
        }
    }
    /**
     * Vertical scroll only if the screen height is less than the threshold
     */
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(vertical = dimensionResource(id = R.dimen._4sdp))
    ) {
        content()
    }
}
