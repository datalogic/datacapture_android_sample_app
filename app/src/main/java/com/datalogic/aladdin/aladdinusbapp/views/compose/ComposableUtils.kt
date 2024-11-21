package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.datalogic.aladdin.aladdinusbapp.R

object ComposableUtils {

    @Composable
    fun HeaderImageView(modifier: Modifier) {
        Row (
            modifier
                .semantics { contentDescription = "app_logo"}
                .fillMaxWidth()
                .padding(top = dimensionResource(id = R.dimen._5sdp)),
            Arrangement.Center,
        ){
            Image(
                modifier = Modifier.size(dimensionResource(id = R.dimen._81sdp), dimensionResource(id = R.dimen._46sdp)),
                painter = painterResource(id = R.drawable.ic_logo_version_screen),
                contentDescription = "app_logo"
            )
        }
    }

    @Composable
    fun FooterImageView(modifier: Modifier) {
        Row (
            modifier
                .semantics { contentDescription = "datalogic_logo"}
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen._15sdp)),
            Arrangement.Center
        ){
            Image(
                painter = painterResource(id = R.drawable.ic_datalogic_logo),
                contentDescription = "app_logo"
            )
        }
    }

    @Composable
    fun CustomButton(modifier: Modifier, buttonState: Boolean, name: String, onClick: () -> Unit) {
        val context = LocalContext.current

        Button(
            modifier = modifier
                .height(dimensionResource(id = R.dimen._50sdp)),
            onClick = onClick,
            enabled = buttonState,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.colorPrimary),
                disabledContainerColor = Color.White
            ),
            elevation = ButtonDefaults.buttonElevation(disabledElevation = dimensionResource(id = R.dimen._50sdp), pressedElevation = dimensionResource(id = R.dimen._50sdp)),
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                color = if (buttonState) colorResource(id = R.color.white)
                else colorResource(id = R.color.colorPrimary)
            )
        }
    }

    @Composable
    fun ShowPopup(onDismiss: () -> Unit) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Alert!", style = MaterialTheme.typography.headlineLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "The selected device type is not supported. Select OEM device", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}