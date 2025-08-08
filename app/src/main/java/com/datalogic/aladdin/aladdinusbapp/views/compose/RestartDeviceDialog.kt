package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel

@Composable
fun RestartDeviceDialog(homeViewModel: HomeViewModel) {
    AlertDialog(
        onDismissRequest = {
            homeViewModel.dismissResetDialog()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    homeViewModel.resetDevice()
                    homeViewModel.dismissResetDialog() // Close dialog after confirming
                }) {
                Text(stringResource(id = R.string.ok))
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.reset_device_title),
                style = MaterialTheme.typography.headlineLarge
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.msg_restart_device),
                style = MaterialTheme.typography.labelLarge
            )
        },
        modifier = Modifier.padding(dimensionResource(id = R.dimen._16sdp)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen._16sdp)),
        containerColor = Color.White,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    )
}