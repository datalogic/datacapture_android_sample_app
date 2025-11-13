package com.datalogic.aladdin.aladdinusbapp.views.activity.scannerScreen

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
//import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils.rememberEnsureBluetoothEnabled
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.LabelCodeTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.LabelIDControlDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.RestartDeviceDialog
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun ScannerScreenPortrait1() {

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

    val content = @Composable {
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
                        //homeViewModel.clearScanData()
                    },
                    colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                ) {
                    Text(
                        text = stringResource(id = R.string.clear_fields),
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))

                if (status == DeviceStatus.OPENED) {
                    LabelIDControlDropdown(
                        modifier = Modifier
                            .semantics { contentDescription = "label_id_control_dropdown" }
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen._55sdp)),
                        homeViewModel.selectedLabelIDControl.observeAsState().value,
                        onLabelIDControlSelected = {
                            homeViewModel.setSelectedLabelIDControl(it)
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
                            homeViewModel.setSelectedLabelCodeType(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._15sdp)))
                }
            }
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