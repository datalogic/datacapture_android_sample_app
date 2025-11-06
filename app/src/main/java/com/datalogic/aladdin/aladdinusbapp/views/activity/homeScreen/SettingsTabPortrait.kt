package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SettingsTabPortrait() {
    val homeViewModel = LocalHomeViewModel.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    /**
     * Define a threshold for vertical scrolling
     * */
    val scrollableThreshold = 500
    val aboutAppModel = homeViewModel.aboutApp.observeAsState(null).value
    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            //About
            Column(
                modifier = Modifier
                    .semantics { contentDescription = "about" }
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen._10sdp))
            ) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_about" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._10sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = "About",
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

                    val cusModifier = Modifier
                        .semantics { contentDescription = "lbl_title" }
                        .width(120.dp)
                        .padding(
                            start = dimensionResource(id = R.dimen._5sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        )

                    ComposableUtils.TextValueRow("Version:", aboutAppModel?.appVersion, cusModifier)
                    ComposableUtils.TextValueRow("SDK Version:", aboutAppModel?.versionSDK, cusModifier)
                    ComposableUtils.TextValueRow(
                        "Android Version:",
                        "${aboutAppModel?.osName} ${aboutAppModel?.osVersion} " +
                                "(SDK ${aboutAppModel?.sdkInt} - ${aboutAppModel?.arch})",
                        cusModifier
                    )
                    ComposableUtils.TextValueRow("Device Name:", "${aboutAppModel?.deviceBrand} ${aboutAppModel?.deviceModel}", cusModifier)
                    ComposableUtils.TextValueRow("Time Zone:", aboutAppModel?.timeZone, cusModifier)
                }
            }
            //Log
            Column(
                modifier = Modifier
                    .semantics { contentDescription = "log" }
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen._10sdp))
            ) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_log" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._10sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = "Log",
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen._15sdp),
                                bottom = dimensionResource(id = R.dimen._5sdp)
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val isDebugEnabled by homeViewModel.isDebugEnabled.observeAsState(false)

                        Switch(
                            checked = isDebugEnabled,
                            onCheckedChange = {
                                homeViewModel.toggleDebug()
                            }
                        )

                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._15sdp)))

                        Text(
                            modifier = Modifier
                                .semantics { contentDescription = "lbl_debug_toggle" }
                                .fillMaxWidth()
                                .padding(
                                    top = dimensionResource(id = R.dimen._15sdp),
                                    bottom = dimensionResource(id = R.dimen._5sdp)
                                ),
                            text = if (isDebugEnabled) "Debug Enabled" else "Debug Disabled",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_save" }
                            .padding(vertical = dimensionResource(id = R.dimen._16sdp))
                            .wrapContentSize(),
                        onClick = {
                            homeViewModel.saveLog()
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text(
                            text = "Save Logs",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    /**
     * Vertical scroll only if the screen height is less than the threshold
     */
    if (screenHeight < scrollableThreshold) {
        Column(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen._3sdp))
                .verticalScroll(rememberScrollState())
        ) {
            content()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}