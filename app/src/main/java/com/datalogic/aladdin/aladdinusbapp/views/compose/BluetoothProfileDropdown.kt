package com.datalogic.aladdin.aladdinusbapp.views.compose

import android.Manifest
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.core.app.ActivityCompat
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.PairingBarcodeType
import com.datalogic.aladdin.aladdinusbapp.utils.PairingStatus
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.BluetoothProfile
import kotlin.collections.contains
import kotlin.collections.forEach


var mCurrentBluetoothProfile by mutableStateOf<PairingBarcodeType?>(null)
val bluetoothProfiles = arrayListOf(
    PairingBarcodeType.SPP,
    PairingBarcodeType.HID,
    PairingBarcodeType.UNLINK
)
@Composable
fun BluetoothProfileDropdown(modifier: Modifier, selectedBluetoothProfile: PairingBarcodeType?,
                             onBluetoothProfileSelected: (PairingBarcodeType?) -> Unit, onButtonStartClicked: (PairingBarcodeType?, Long) -> Unit ){
    var mExpanded by remember { mutableStateOf(false) }
    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}
    var profile = ""

    val homeViewModel = LocalHomeViewModel.current
    val currentConnectionStatus = homeViewModel.currentPairingStatus.observeAsState(null).value


    LaunchedEffect(selectedBluetoothProfile, bluetoothProfiles) {
        mCurrentBluetoothProfile = selectedBluetoothProfile
        if ((mCurrentBluetoothProfile == null && bluetoothProfiles.isNotEmpty()) ||
            (mCurrentBluetoothProfile != null && bluetoothProfiles.isNotEmpty() && !bluetoothProfiles.contains(mCurrentBluetoothProfile))) {
            mCurrentBluetoothProfile = bluetoothProfiles.first()
            onBluetoothProfileSelected(mCurrentBluetoothProfile)
        } else if (mCurrentBluetoothProfile != null && bluetoothProfiles.isEmpty()) {
            mCurrentBluetoothProfile = null
            onBluetoothProfileSelected(mCurrentBluetoothProfile)
        }
    }

    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
    val itemHeight = 56.dp
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen._8sdp))
    ) {

        // 1) Dropdown (takes most width)
        Card(
            shape = RoundedCornerShape(dimensionResource(id = R.dimen._5sdp)),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen._10sdp)),
            colors = CardDefaults.cardColors(Color.White),
            modifier = Modifier
                .weight(0.35f)
                .height(itemHeight) // <-- adjust ratio
        ) {
            if (selectedBluetoothProfile != null) {
                profile = when (selectedBluetoothProfile) {
                    PairingBarcodeType.SPP -> "SPP"
                    PairingBarcodeType.HID -> "HID"
                    PairingBarcodeType.UNLINK -> "Unlink"
                }
            }

            TextField(
                value = profile,
                textStyle = MaterialTheme.typography.labelLarge,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        mTextFieldSize = coordinates.size.toSize()
                    }
                    .clickable { mExpanded = !mExpanded }
                    .fillMaxHeight(),
                trailingIcon = {
                    Icon(icon, stringResource(id = R.string.arrow_dropdown), tint = Color.Black)
                },
                enabled = false,
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )

            if (bluetoothProfiles.isNotEmpty()) {
                DropdownMenu(
                    expanded = mExpanded,
                    onDismissRequest = { mExpanded = false },
                    modifier = Modifier
                        .background(color = Color.White)
                        .width(with(LocalDensity.current) { mTextFieldSize.width.toDp() })
                ) {
                    bluetoothProfiles.forEach { bluetoothProfileElem ->
                        val itemText = when (bluetoothProfileElem) {
                            PairingBarcodeType.SPP -> "SPP"
                            PairingBarcodeType.HID -> "HID"
                            PairingBarcodeType.UNLINK -> "Unlink"
                        }

                        DropdownMenuItem(
                            text = { Text(itemText, style = MaterialTheme.typography.labelLarge) },
                            onClick = {
                                onBluetoothProfileSelected(bluetoothProfileElem)
                                mCurrentBluetoothProfile = bluetoothProfileElem
                                mExpanded = false
                            }
                        )
                    }
                }
            }
        }
        val focusManager = LocalFocusManager.current
        val timeoutFocusRequester = remember { FocusRequester() }
        // 2) Timeout (smaller)
        var timeout by remember { mutableStateOf("10") }
        OutlinedTextField(
            modifier = Modifier
                .semantics { contentDescription = "otf_timeout" }
                .weight(0.35f)
                .height(itemHeight)
                .focusRequester(timeoutFocusRequester), // match material height
            value = timeout,
            onValueChange = { timeout = it },
            label = { Text("Timeout (s)") },
            singleLine = true,
            maxLines = 1,

            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // 3) Start button (smallest)
        Button(
            modifier = Modifier
                .semantics { contentDescription = "btn_start_scan" }
                .weight(0.3f)          // <-- flexible, ensures it fits
                .height(itemHeight),
            onClick = {
                focusManager.clearFocus(force = true)
                val timeoutLong = timeout.trim().toLongOrNull() ?: 10L
                onButtonStartClicked(mCurrentBluetoothProfile, timeoutLong)
            },
            colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary)),
            enabled = (selectedBluetoothProfile != PairingBarcodeType.UNLINK &&
                    currentConnectionStatus != PairingStatus.Scanning)
        ) {
            Text(text = "Start", color = Color.White, maxLines = 1)
        }
    }

}