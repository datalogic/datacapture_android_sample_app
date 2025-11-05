package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel

@Composable
fun ManualDetect(){
    val homeViewModel = LocalHomeViewModel.current
    val selectedUsbDevice = homeViewModel.selectedUsbDevice.observeAsState(null).value
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