package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.app.Activity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
//import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils.rememberEnsureBluetoothEnabled
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.devicesScreen.DeviceListScreen
import com.datalogic.aladdin.aladdinusbapp.views.compose.ConnectionTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.LoggingDropdown
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
    val isBluetoothEnabled = homeViewModel.isBluetoothEnabled.observeAsState(false).value

    val selectedBluetoothDevice = homeViewModel.selectedBluetoothDevice.observeAsState(null).value
    val allBluetoothDevices = homeViewModel.allBluetoothDevices.observeAsState(ArrayList()).value

    val usbDeviceList = homeViewModel.usbDeviceList.observeAsState(ArrayList()).value
    val selectedUsbDevice = homeViewModel.selectedUsbDevice.observeAsState(null).value
    val isEnableScale = homeViewModel.isEnableScale.observeAsState(false).value
    val isScaleAvailable = homeViewModel.isScaleAvailable.observeAsState(false).value

    val context = LocalContext.current
    val activity = context as? Activity
//    val ensureBluetoothEnabled = rememberEnsureBluetoothEnabled(context)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen._4sdp))
        ) {
            LoggingDropdown()
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
            DeviceListScreen()
        }
    }
}
