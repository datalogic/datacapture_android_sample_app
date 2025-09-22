package com.datalogic.aladdin.aladdinusbapp.views.compose

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.app.ActivityCompat
import com.datalogic.aladdin.aladdinusbapp.R

@Composable
fun BluetoothDeviceDropdown(
    modifier: Modifier,
    selectedBluetoothDevice: BluetoothDevice?,
    bluetoothDevices: List<BluetoothDevice>,
    onBluetoothDeviceSelected: (BluetoothDevice?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val context = LocalContext.current
    var deviceDisplayName = ""

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.d(TAG, "[BluetoothDeviceDropdown] permission denied")
    } else if (selectedBluetoothDevice != null){
        deviceDisplayName = "${selectedBluetoothDevice.name}"
    } else {
        deviceDisplayName = "No device selected"
    }

    LaunchedEffect(bluetoothDevices) {
        if (selectedBluetoothDevice != null && !bluetoothDevices.contains(selectedBluetoothDevice)) {
            onBluetoothDeviceSelected(null)
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
    ) {
        TextField(
            value = deviceDisplayName,
            onValueChange = {},
            textStyle = MaterialTheme.typography.labelLarge,
            enabled = false,
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.arrow_dropdown),
                    tint = Color.Black
                )
            },
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .clickable { expanded = !expanded },
            colors = TextFieldDefaults.colors(
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        DropdownMenu(
            expanded = expanded && bluetoothDevices.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            bluetoothDevices.forEach { device ->
                DropdownMenuItem(
                    text = { Text(text = device.name ?: "Unknown", style = MaterialTheme.typography.labelLarge) },
                    onClick = {
                        Toast.makeText(
                            context,
                            "Selected device ${device.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBluetoothDeviceSelected(device)
                        expanded = false
                    }
                )
            }
        }
    }
}