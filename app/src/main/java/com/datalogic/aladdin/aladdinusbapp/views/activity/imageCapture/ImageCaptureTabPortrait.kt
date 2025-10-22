package com.datalogic.aladdin.aladdinusbapp.views.activity.imageCapture

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel


@Composable
fun ImageCaptureTabPortrait() {
    val imageCaptureModel = LocalHomeViewModel.current

    // Initialize sliders with the current values from the ViewModel
    var brightness by remember {
        mutableStateOf(imageCaptureModel.getBrightnessPercentage().toFloat())
    }
    var contrast by remember {
        mutableStateOf(imageCaptureModel.getContrastPercentage().toFloat())
    }
    var previewImage by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(imageCaptureModel) {
        imageCaptureModel.setImageCallback { bitmap ->
            previewImage = bitmap
        }
        onDispose {
            imageCaptureModel.setImageCallback(null)
        }
    }
    Text(
        modifier = Modifier
            .semantics { contentDescription = "lbl_imageCapture" }
            .fillMaxWidth()
            .padding(bottom = dimensionResource(id = R.dimen._5sdp)),
        text = stringResource(id = R.string.imageCapture),
        style = MaterialTheme.typography.headlineLarge,
        fontSize = 20.sp,
        textAlign = TextAlign.Center
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (imageCaptureModel.selectedDevice.value
                ?.deviceType
                ?.name
                ?.equals("HHS") == true) {
            SliderRow(
                "Brightness",
                brightness,
                0f,
                100f
            ) { newValue ->
                brightness = newValue
                imageCaptureModel.setBrightness(newValue.toInt())
            }

            SliderRow(
                "Contrast",
                contrast,
                0f,
                100f
            ) { newValue ->
                contrast = newValue
                imageCaptureModel.setContrast(newValue.toInt())
            }
        }

        // You can similarly implement Image Format if needed.
        CaptureButtons(imageCaptureModel, onBeforeCapture = { previewImage = null })

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        )
        {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (previewImage != null) {
                    // Display the image
                    Image(
                        bitmap = previewImage!!.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(text = "Image Preview", color = Color.DarkGray)
                }
            }
        }

    }
}

@Composable
fun SliderRow(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$label: ${value.toInt()}",
            color = colorResource(id = R.color.colorPrimary)
        )
        Slider(value = value, onValueChange = onValueChange, valueRange = min..max)
        // Use ToggleableButton for the reset functionality.
        ToggleableButton(label = "Reset $label", onClick = { onValueChange(50f) })
    }
}

@Composable
fun DropdownRow(
    label: String,
    options: List<String>,
    selected: String,
    onSelection: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Here we add toggle effect on the dropdown's trigger button.
    ToggleableButton(
        label = "Reset $label",
        onClick = { expanded = true }
    ) {
        Text("$label: $selected", color = Color.Black)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                onClick = {
                    onSelection(option)
                    expanded = false
                },
                text = { Text(option) }
            )
        }
    }
}

@Composable
fun CaptureButtons(model: HomeViewModel, onBeforeCapture: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val isLoading by model.isLoading.observeAsState(false)

        listOf("Capture Auto").forEach { label ->
            // For Capture Auto we drive the visual state from the ViewModel's isLoading
            val controlledSelected: Boolean? = when (label) {
                "Capture Auto" -> isLoading
                else -> null
            }

            ToggleableButton(label = label, onClick = {
                when(label) {
                    "Capture Auto" -> {
                        onBeforeCapture()
                        model.startCaptureAuto()
                    }
                }
            }, selected = controlledSelected)
        }
    }
}
@Composable
fun ToggleableButton(
    label: String,
    onClick: () -> Unit,
    selected: Boolean? = null,
    defaultColor: Color = Color.Gray,
    selectedColor: Color = colorResource(id = R.color.colorPrimary),
    defaultTextColor: Color = Color.Black,
    selectedTextColor: Color = Color.White,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    var internalSelected by remember { mutableStateOf(false) }
    val isSelected = selected ?: internalSelected

    Button(
        onClick = {
            onClick()
            if (selected == null) internalSelected = !internalSelected
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) selectedColor else defaultColor
        ),
        modifier = modifier
    ) {
        if (content != null) {
            content()
        } else {
            Text(text = label, color = if (isSelected) selectedTextColor else defaultTextColor)
        }
    }
}
@Preview
@Composable
fun PreviewImageCaptureTabPortrait() {
    ImageCaptureTabPortrait()
}

fun captureAuto(){
    println("Auto capture button clicked")

}
fun captureOnTrigger(){
    println("On Trigger capture button clicked")
}