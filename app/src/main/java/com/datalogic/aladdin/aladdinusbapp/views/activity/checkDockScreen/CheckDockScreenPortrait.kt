package com.datalogic.aladdin.aladdinusbapp.views.activity.checkDockScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceDockStatus

@Composable
fun CheckDockScreenPortrait() {
    val homeViewModel = LocalHomeViewModel.current
    val deviceStates by homeViewModel.deviceCradleStates.collectAsState()
    val isCheckDockingEnabled by homeViewModel.isCheckDockingEnabled.observeAsState(false)

    val content = @Composable {
        Column(
            modifier = Modifier
                .semantics { contentDescription = "scanner_data" }
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen._10sdp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen._15sdp),
                        bottom = dimensionResource(id = R.dimen._5sdp)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Switch(
                    checked = isCheckDockingEnabled,
                    onCheckedChange = {
                        homeViewModel.toggleCheckDocking()
                    },
                    modifier = Modifier
                        .padding(
                            top = dimensionResource(id = R.dimen._15sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        )
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._15sdp)))

                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_docking_toggle" }
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(id = R.dimen._15sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = if (isCheckDockingEnabled) "Check Docking Enabled" else "Check Docking Disabled",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            if (isCheckDockingEnabled) {
                if (deviceStates.isNotEmpty()) {
                    for (state in deviceStates) {
                        DeviceDockStatus(state)
                    }
                } else {
                    Text(
                        modifier = Modifier
                            .semantics { contentDescription = "lbl_empty" }
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(id = R.dimen._15sdp),
                                bottom = dimensionResource(id = R.dimen._5sdp)
                            ),
                        text = "No Device",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
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