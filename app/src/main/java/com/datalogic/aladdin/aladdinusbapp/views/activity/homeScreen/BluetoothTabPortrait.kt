package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.app.Activity
import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.PairingStatus
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.BluetoothProfileDropdown


@Composable
fun BluetoothTabPortrait() {
    val homeViewModel = LocalHomeViewModel.current

    val qrBitmap by homeViewModel.qrBitmap.observeAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val context = LocalContext.current
    val activity = context as? Activity
    val selectedBluetoothProfile = homeViewModel.selectedBluetoothProfile.observeAsState(null).value
    val currentConnectionStatus = homeViewModel.currentPairingStatus.observeAsState(null).value
    val currentBleName = homeViewModel.currentBleDeviceName.observeAsState(null).value

    /**
     * Define a threshold for vertical scrolling
     * */
    val scrollableThreshold = 500
    val content = @Composable {
        BluetoothProfileDropdown(
            modifier = Modifier
                .semantics { contentDescription = "bluetooth_device_list_dropdown" }
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen._55sdp)),
            selectedBluetoothProfile,
            onBluetoothProfileSelected = {
                if (it != null) {
                    homeViewModel.setSelectedBluetoothDevice(it)
                    homeViewModel.setPairingStatus(PairingStatus.Idle)
                }
            },
            onButtonStartClicked = {
                if (it != null)
                    homeViewModel.createQrCode(it)
            }
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            when (currentConnectionStatus) {
                PairingStatus.Idle -> {
                    Text(
                        text = "Choose Bluetooth profile and tap \"Start\" to continue.",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                PairingStatus.Scanning -> {
                    Text(
                        text = stringResource(R.string.scan_to_pair),
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(180.dp)
                        )
                        activity?.let {
                            Log.d(ContentValues.TAG, "[BluetoothTabPortrait]  scanBluetoothDevice")
                            homeViewModel.scanBluetoothDevice(it)
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(210.dp)
                                .background(Color.LightGray)
                        )
                    }
                }
                PairingStatus.Connected -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pairing_illustration_success_icon),
                        contentDescription = "Connect success",
                        modifier = Modifier.size(280.dp, 100.dp)
                    )
                    val deviceName = if (currentBleName?.isNotEmpty() == true) {
                        "Device $currentBleName connected successfully."
                    } else {
                        "Device connected successfully."
                    }
                    Text(
                        text = deviceName,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                PairingStatus.Paired -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pairing_illustration_success_icon),
                        contentDescription = "Connect success",
                        modifier = Modifier.size(280.dp, 100.dp)
                    )
                    val deviceName = if (currentBleName?.isNotEmpty() == true) {
                        "Device $currentBleName paired successfully."
                    } else {
                        "Device paired successfully."
                    }
                    Text(
                        text = deviceName,
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                PairingStatus.Error -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pairing_illustration_error_icon),
                        contentDescription = "Connect failed",
                        modifier = Modifier.size(280.dp, 100.dp)
                    )
                    Text(
                        text = stringResource(R.string.device_pairing_fail),
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                PairingStatus.Timeout -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pairing_illustration_error_icon),
                        contentDescription = "Connect failed",
                        modifier = Modifier.size(280.dp, 100.dp)
                    )
                    Text(
                        text = "Discovery process is stopped. Please try again.",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
                PairingStatus.PermissionDenied -> {
                    homeViewModel.setPairingStatus(PairingStatus.Idle)
                    if (selectedBluetoothProfile != null) {
                        homeViewModel.createQrCode(selectedBluetoothProfile)
                    }
                }
                null -> {}
            }
        }
    }

    /**
     * Vertical scroll only if the screen height is less than the threshold
     */
    if (screenHeight < scrollableThreshold) {
        Column(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen._3sdp))
            .verticalScroll(rememberScrollState())) {
            content()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}