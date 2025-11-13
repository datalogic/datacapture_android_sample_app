package com.datalogic.aladdin.aladdinusbapp.views.activity.scaleScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

/*@Composable

fun scaleScreenPortrait() {
    val homeViewModel = LocalHomeViewModel.current
    val scaleStatus by homeViewModel.scaleStatus.observeAsState("")
    val scaleWeight by homeViewModel.scaleWeight.observeAsState("")
    val scaleUnit by homeViewModel.scaleUnit.observeAsState("")

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
        }
    }
}*/
