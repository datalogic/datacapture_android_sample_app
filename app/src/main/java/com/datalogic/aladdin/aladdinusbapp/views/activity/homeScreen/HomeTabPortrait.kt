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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun HomeTabPortrait() {

    val homeViewModel = LocalHomeViewModel.current
    val deviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val status = homeViewModel.status.observeAsState(DeviceStatus.NONE).value

    val scanLabel = homeViewModel.scanLabel.observeAsState("").value
    val scanData = homeViewModel.scanData.observeAsState("").value
    val selectedDevice = homeViewModel.selectedDevice.observeAsState(null).value


    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    /**
     * Define a threshold for vertical scrolling
     * */
    val scrollableThreshold = 500

    val content = @Composable {

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
                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(dimensionResource(id = R.dimen._8sdp)))
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
                        .height(dimensionResource(id = R.dimen._36sdp))
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(dimensionResource(id = R.dimen._8sdp)))
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
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(dimensionResource(id = R.dimen._8sdp)))
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

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CustomButton(
                modifier = Modifier
                    .weight(0.5f)
                    .semantics { contentDescription = "btn_open" },
                buttonState = (status == DeviceStatus.CLOSED && deviceList.isNotEmpty()),
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
    }

    /**
     * Vertical scroll only if the screen height is less than the threshold
     */
    if (screenHeight < scrollableThreshold) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(vertical = dimensionResource(id = R.dimen._4sdp))) {
            content()
        }
    } else {
        content()
    }
}