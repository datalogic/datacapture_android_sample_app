package com.datalogic.aladdin.aladdinusbapp.views.activity.imageCapture

import android.graphics.Bitmap
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel


@Composable
fun ImageCaptureTabPortrait() {
    val imageCaptureModel = LocalHomeViewModel.current
    var brightness by remember { mutableStateOf(0f) }
    var contrast by remember { mutableStateOf(0f) }
    var sensorMode by remember { mutableStateOf("Auto") }
    var previewImage by remember { mutableStateOf<Bitmap?>(null) }

    DisposableEffect(imageCaptureModel) {
        imageCaptureModel.setImageCallback { bitmap ->
            if (bitmap != null) {
                previewImage = bitmap
            }
        }
        onDispose {
            imageCaptureModel.setImageCallback(null)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SliderRow("Brightness", brightness, -100f, 100f) { brightness = it }
        SliderRow("Contrast", contrast, -100f, 100f) { contrast = it }
        DropdownRow(
            label = "Sensor Mode",
            options = listOf("Auto", "Sensor 1", "Sensor 2"),
            selected = sensorMode
        ) { sensorMode = it }
        // You can similarly implement Image Format if needed.
        CaptureButtons(imageCaptureModel)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.LightGray)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (previewImage != null) {
                // Display the image
                androidx.compose.foundation.Image(
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

@Composable
fun SliderRow(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$label: ${value.toInt()}",
            color = colorResource(id = R.color.colorPrimary)
        )
        Slider(value = value, onValueChange = onValueChange, valueRange = min..max)
        // Use ToggleableButton for the reset functionality.
        ToggleableButton(label = "Reset $label", onClick = { onValueChange(0f) })
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
    var isSelected by remember { mutableStateOf(false) }
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
fun CaptureButtons(model: HomeViewModel) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        listOf("Auto", "On Trigger").forEach { label ->
            // Each capture button uses the ToggleableButton with its own toggle state.
            ToggleableButton(label = label, onClick = {
                when(label) {
                    "Auto" -> model.startCaptureAuto()
                    "On Trigger" -> model.startCaptureOnTrigger()
                }
            })
        }
    }
}
@Composable
fun ToggleableButton(
    label: String,
    onClick: () -> Unit,
    defaultColor: Color = Color.Gray,
    selectedColor: Color = colorResource(id = R.color.colorPrimary),
    defaultTextColor: Color = Color.Black,
    selectedTextColor: Color = Color.White,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    var isSelected by remember { mutableStateOf(false) }
    Button(
        onClick = {
            onClick()
            isSelected = !isSelected
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


