package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import kotlinx.coroutines.delay

object ComposableUtils {

    @Composable
    fun HeaderImageView(modifier: Modifier) {
        val homeViewModel = LocalHomeViewModel.current
        val aboutAppModel = homeViewModel.aboutApp.observeAsState(null).value
        val version = aboutAppModel?.appVersion ?: ""
        val versionApp = if (version.isEmpty()) "" else "V $version"
        val configuration = LocalConfiguration.current
        if (configuration.screenHeightDp > 350) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen._5sdp))
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Image(
                        modifier = Modifier
                            .size(
                                width = dimensionResource(id = R.dimen._81sdp),
                                height = dimensionResource(id = R.dimen._46sdp)
                            ),
                        painter = painterResource(id = R.drawable.ic_logo_version_screen),
                        contentDescription = "app_logo"
                    )

                    Text(
                        modifier = Modifier
//                            .padding(start = 4.dp)
                            .semantics { contentDescription = "lbl_version" },
                        text = versionApp,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }

        }
    }

    @Composable
    fun FooterImageView(modifier: Modifier) {
        val configuration = LocalConfiguration.current
        if (configuration.screenHeightDp > 350) {
            Row(
                modifier
                    .semantics { contentDescription = "datalogic_logo" }
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen._15sdp)),
                Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_datalogic_logo),
                    contentDescription = "datalogic_logo"
                )
            }
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
            elevation = ButtonDefaults.buttonElevation(disabledElevation = dimensionResource(id = R.dimen._10sdp), pressedElevation = dimensionResource(id = R.dimen._10sdp)),
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
    fun CustomButtonRow(modifier: Modifier, openState: Boolean, name: String, onClick: () -> Unit) {
        Button(
            modifier = modifier
                .height(dimensionResource(id = R.dimen._50sdp)),
            onClick = onClick,
            colors = if(openState) ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.white),
                disabledContainerColor = colorResource(id = R.color.colorPrimary)
            ) else ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.colorPrimary),
                disabledContainerColor = colorResource(R.color.white)),
            elevation = ButtonDefaults.buttonElevation(disabledElevation = dimensionResource(id = R.dimen._4sdp),
                pressedElevation = dimensionResource(id = R.dimen._4sdp)),
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                color = if (openState) colorResource(id = R.color.colorPrimary)
                else colorResource(id = R.color.white)
            )
        }
    }

    @Composable
    fun ShowPopup(alert: Boolean, onDismiss: () -> Unit, msg: String) {
        if (alert) {
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
                        Text(
                            text = stringResource(id = R.string.alert),
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._8sdp)))
                        Text(text = msg, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._16sdp)))
                    }
                }
            }
            LaunchedEffect(Unit) {
                delay(2000)
                onDismiss()
            }
        }
    }

    @Composable
    fun ShowLoading(onDismiss: () -> Unit) {
        Dialog(onDismissRequest = onDismiss) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.Blue,
                    strokeWidth = dimensionResource(id = R.dimen._4sdp)
                )
                Text(text = stringResource(id = R.string.processing))
            }
        }
    }

    @Composable
    fun ShowPercentLoading(onDismiss: () -> Unit, percent: String) {
        Dialog(onDismissRequest = onDismiss) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.Blue,
                    strokeWidth = dimensionResource(id = R.dimen._4sdp)
                )
                Text(text = percent)
            }
        }
    }

    @Composable
    fun CustomSwitch(checkedState: Boolean, onClick: (Boolean) -> Unit) {
        Switch(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen._4sdp))
                .height(dimensionResource(id = R.dimen._25sdp))
                .width(dimensionResource(id = R.dimen._46sdp)),
            checked = checkedState,
            onCheckedChange = { newState -> onClick(newState) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorResource(id = R.color.white),
                checkedBorderColor = colorResource(id = R.color.border_green),
                checkedTrackColor = colorResource(id = R.color.track_green),
                uncheckedThumbColor = colorResource(id = R.color.white),
                uncheckedBorderColor = colorResource(id = R.color.border_red),
                uncheckedTrackColor = colorResource(id = R.color.track_red)
            ),
            thumbContent = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    tint = colorResource(id = R.color.white),
                    contentDescription = null,
                    modifier = Modifier.padding(2.dp)
                )
            },
        )
    }
    
    /**
     * A custom TextField component used throughout the application.
     * 
     * @param textValue The current text value to display.
     * @param onValueChange Callback when the text value changes.
     * @param readOnly Whether the text field should be read-only.
     * @param labelText Label text to display above the text field.
     * @param enabledStatus Whether the text field is enabled.
     * @param keyboardType Optional keyboard type (defaults to Text).
     */
    @Composable
    fun CustomTextField(
        textValue: String,
        onValueChange: (String) -> Unit,
        readOnly: Boolean,
        labelText: String,
        enabledStatus: Boolean,
        keyboardType: KeyboardType = KeyboardType.Text
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
        ) {
            OutlinedTextField(
                value = textValue,
                onValueChange = onValueChange,
                readOnly = readOnly,
                label = { Text(labelText) },
                enabled = enabledStatus,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = colorResource(id = R.color.colorPrimary),
                    unfocusedIndicatorColor = colorResource(id = R.color.colorPrimary),
                    focusedLabelColor = colorResource(id = R.color.colorPrimary),
                    unfocusedLabelColor = colorResource(id = R.color.colorPrimary),
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true
            )
        }
    }

    @Composable
    fun TextValueRow(title: String, value: String?, customModifier: Modifier? = null){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp),
        ) {
            if (customModifier != null) {
                Text(
                    modifier = customModifier,
                    text = title,
                    style = MaterialTheme.typography.headlineLarge
                )
            } else {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_title" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._5sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = title,
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            if (value != null) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_value" }
                        .fillMaxWidth()
                        .padding(
                            start = dimensionResource(id = R.dimen._10sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = value,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
        }
    }
}