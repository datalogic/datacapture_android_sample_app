package com.datalogic.aladdin.aladdinusbapp.views.compose

import android.app.Activity
import android.hardware.usb.UsbDevice
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.CradleState
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun DeviceDockStatus(
    state: CradleState
) {
    Column(
        modifier = Modifier
            .semantics { contentDescription = "device" }
            .fillMaxWidth()
            .padding(vertical = dimensionResource(id = R.dimen._10sdp))
    ) {
        Text(
            modifier = Modifier
                .semantics { contentDescription = "lbl_device_name" }
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(id = R.dimen._10sdp),
                    bottom = dimensionResource(id = R.dimen._5sdp)
                ),
            text = "Device: ${state.device.displayName}",
            style = MaterialTheme.typography.headlineLarge
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    BorderStroke(1.dp, Color.Black),
                    RoundedCornerShape(dimensionResource(id = R.dimen._8sdp))
                )
                .padding(horizontal = dimensionResource(id = R.dimen._16sdp), vertical = dimensionResource(id = R.dimen._16sdp)),
        ) {
            val cusModifier = Modifier
                .semantics { contentDescription = "lbl_title" }
                .width(150.dp)
                .padding(
                    start = dimensionResource(id = R.dimen._5sdp),
                    bottom = dimensionResource(id = R.dimen._5sdp)
                )
            ComposableUtils.TextValueRow("Device Docking:", if (state.docked) "Yes" else "No",cusModifier)
//            ComposableUtils.TextValueRow("Device Linking:", if (state.linked) "Yes" else "No",cusModifier)
        }
    }
}