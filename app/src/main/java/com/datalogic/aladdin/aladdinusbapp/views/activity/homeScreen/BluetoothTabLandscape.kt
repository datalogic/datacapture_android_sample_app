package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.annotation.SuppressLint
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.PairingBarcodeType
import com.datalogic.aladdin.aladdinusbapp.utils.PairingStatus
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.BluetoothProfileDropdown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun BluetoothTabLandscape() {
    val homeViewModel = LocalHomeViewModel.current
    val qrBitmap by homeViewModel.qrBitmap.observeAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val context = LocalContext.current
    val activity = context as? Activity
    val selectedBluetoothProfile = homeViewModel.selectedBluetoothProfile.observeAsState(null).value
    val previousProfile = homeViewModel.previousBluetoothProfile.observeAsState(null).value
    val currentConnectionStatus = homeViewModel.currentPairingStatus.observeAsState(null).value
    val currentBleName = homeViewModel.currentBleDeviceName.observeAsState(null).value

    val scrollState = rememberScrollState()
    val unlinkImageOffset = remember { mutableIntStateOf(0) }
    val qrImageOffset = remember { mutableIntStateOf(0) }

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
                    homeViewModel.setSelectedBluetoothProfile(it)
                    homeViewModel.setPairingStatus(PairingStatus.Idle)
                }
            },
            onButtonStartClicked = {
                Log.e("BluetoothTabLandscape", "onButtonStartClicked on profile: $it")
                activity?.let { activity ->
                    if (it != null)
                        homeViewModel.createQrCode(it, activity)
                }
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
                    if (selectedBluetoothProfile != PairingBarcodeType.UNLINK) {
                        Text(
                            text = "Choose Bluetooth profile and tap \"Start\" to continue.",
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.scan_unlink_barcode),
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Image(
                            painter = painterResource(id = R.drawable.unlink),
                            contentDescription = "Unlink code",
                            modifier = Modifier
                                .size(280.dp, 100.dp)
                                .onGloballyPositioned { coordinates ->
                                    unlinkImageOffset.intValue =
                                        coordinates.positionInParent().y.toInt()
                                }
                        )
                        LaunchedEffect(currentConnectionStatus) {
                            Log.d(
                                ContentValues.TAG,
                                "[BluetoothTabLandscape] unlinkImageOffset value: ${unlinkImageOffset.intValue}"
                            )
                            if (unlinkImageOffset.intValue > 0) {
                                val value = unlinkImageOffset.intValue + 50
                                scrollState.animateScrollTo(value)
                            }
                        }

                        Log.e("BluetoothTabLandscape", "previousProfile: $previousProfile")
                        if (previousProfile != null && previousProfile != PairingBarcodeType.UNLINK) {
                            Button(
                                modifier = Modifier
                                    .semantics { contentDescription = "btn_got_back" }
                                    .padding(start = dimensionResource(id = R.dimen._8sdp)),
                                onClick = {
                                    homeViewModel.setSelectedBluetoothProfile(previousProfile)
                                    homeViewModel.setPairingStatus(PairingStatus.Idle)
                                    activity?.let { activity ->
                                        homeViewModel.createQrCode(previousProfile, activity)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                            ) {
                                Text(
                                    text = "Pair",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                PairingStatus.Scanning -> {
                    if (qrBitmap != null) {
                        Text(
                            text = stringResource(R.string.scan_to_pair),
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(160.dp)
                                .onGloballyPositioned { coordinates ->
                                    qrImageOffset.intValue =
                                        coordinates.positionInParent().y.toInt()
                                }
                        )
                        LaunchedEffect(currentConnectionStatus) {
                            Log.d(
                                ContentValues.TAG,
                                "[BluetoothTabLandscape] qrImageOffset value: ${qrImageOffset.intValue}"
                            )
                            if (qrImageOffset.intValue > 0) {
                                val value = qrImageOffset.intValue + 60 + 16
                                scrollState.animateScrollTo(value)
                            }
                        }

                    } else {
                        Text(
                            text = "Created barcode unsuccessfully. Tap \"Start\" to try again.",
                            fontSize = 16.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .size(160.dp)
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
                        modifier = Modifier.size(240.dp, 80.dp)
                    )
                    Text(
                        text = stringResource(R.string.device_pairing_fail),
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_goto_unlink" }
                            .padding(start = dimensionResource(id = R.dimen._8sdp)),
                        onClick = {
                            homeViewModel.setPreviousBluetoothProfile(selectedBluetoothProfile)
                            homeViewModel.setSelectedBluetoothProfile(PairingBarcodeType.UNLINK)
                            homeViewModel.setPairingStatus(PairingStatus.Idle)
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text(
                            text = "Unlink",
                            color = Color.White
                        )
                    }
                }

                PairingStatus.Timeout -> {
                    Image(
                        painter = painterResource(id = R.drawable.ic_pairing_illustration_error_icon),
                        contentDescription = "Connect failed",
                        modifier = Modifier.size(280.dp, 100.dp)
                    )
//                    Text(
//                        text = "Discovery process is stopped. Please try again.",
//                        fontSize = 16.sp,
//                        color = Color.Black,
//                        textAlign = TextAlign.Center
//                    )
                    Text(
                        text = stringResource(R.string.device_pairing_fail),
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_goto_unlink" }
                            .padding(start = dimensionResource(id = R.dimen._8sdp)),
                        onClick = {
                            homeViewModel.setPreviousBluetoothProfile(selectedBluetoothProfile)
                            homeViewModel.setSelectedBluetoothProfile(PairingBarcodeType.UNLINK)
                            homeViewModel.setPairingStatus(PairingStatus.Idle)
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text(
                            text = "Unlink",
                            color = Color.White
                        )
                    }
                }

                PairingStatus.PermissionDenied -> {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(3000)
                        activity?.let { activity ->
                            homeViewModel.scanBluetoothDevice( activity)
                            homeViewModel.setPairingStatus(PairingStatus.Scanning)
                        }
                    }
                    Text(
                        text = "Permission required!",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Image(
                        painter = painterResource(id = R.drawable.ic_pairing_illustration_error_icon),
                        contentDescription = "Connect failed",
                        modifier = Modifier.size(280.dp, 100.dp)
                    )
                    Text(
                        text = "Please enable Bluetooth & Location, grant permissions, then try again.",
                        fontSize = 16.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                null -> {}
            }
        }
    }

    /**
     * Vertical scroll only if the screen height is less than the threshold
     */
    if (screenHeight < scrollableThreshold) {
        Column(
            modifier = Modifier
                .padding(vertical = dimensionResource(id = R.dimen._1sdp))
                .verticalScroll(scrollState)
        ) {
            content()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}