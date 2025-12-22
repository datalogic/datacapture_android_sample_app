package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

//import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils.rememberEnsureBluetoothEnabled
import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.devicesScreen.BluetoothDeviceItem
import com.datalogic.aladdin.aladdinusbapp.views.activity.devicesScreen.DeviceRow
import com.datalogic.aladdin.aladdinusbapp.views.compose.LoggingDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun HomeTabPortrait() {

    val homeViewModel = LocalHomeViewModel.current

    val autoDetectChecked = homeViewModel.autoDetectChecked.observeAsState(true).value
    val allBluetoothDevices = homeViewModel.allBluetoothDevices.observeAsState(ArrayList()).value

    val dlDeviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val usbDeviceList = homeViewModel.usbDeviceList.observeAsState(ArrayList()).value

    val context = LocalContext.current
    val activity = context as? Activity
//    val ensureBluetoothEnabled = rememberEnsureBluetoothEnabled(context)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = dimensionResource(id = R.dimen._4sdp)),
        contentPadding = PaddingValues(bottom = dimensionResource(id = R.dimen._10sdp)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen._8sdp))
    ) {
        item { LoggingDropdown() }

        // Auto-detect row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = dimensionResource(id = R.dimen._15sdp),
                        bottom = dimensionResource(id = R.dimen._5sdp)
                    ),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Switch(
                    checked = autoDetectChecked,
                    onCheckedChange = { homeViewModel.setAutoDetectChecked(it) }
                )
                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._15sdp)))
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_auto_detect_device" }
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(id = R.dimen._15sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = stringResource(id = R.string.auto_detect_device),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        if(autoDetectChecked) {
            // DL USB devices section
            if (dlDeviceList.isNotEmpty()) {
                items(dlDeviceList, key = { it.id }) { device ->
                    DeviceRow(
                        dlDevice = device,
                        usbDevice = null
                    ) // from DevicesScreen.kt (below)
                }
            }
        } else {
            // USB devices section
            if (usbDeviceList.isNotEmpty()) {
                itemsIndexed(usbDeviceList, key = { _, it -> it.deviceId }) { _, device ->
                    var tempDlDevice: DatalogicDevice? = null
                    if (dlDeviceList.isNotEmpty()) {
                        tempDlDevice = dlDeviceList.firstOrNull { checkDlDevice -> checkDlDevice.usbDevice.deviceName == device.deviceName &&
                                checkDlDevice.status.value == DeviceStatus.OPENED }
                    }

                    DeviceRow(dlDevice = tempDlDevice, usbDevice = device)
                }
            }
        }

        // Bluetooth devices section
        if (allBluetoothDevices.isNotEmpty()) {
            items(allBluetoothDevices, key = { it.id }) { device ->
                BluetoothDeviceItem(device) // from DevicesScreen.kt (below)
            }
        }
    }
}
