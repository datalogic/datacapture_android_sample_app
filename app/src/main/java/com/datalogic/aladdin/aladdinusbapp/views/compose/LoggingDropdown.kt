package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel

@Composable
fun LoggingDropdown() {
    val homeViewModel = LocalHomeViewModel.current
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
}