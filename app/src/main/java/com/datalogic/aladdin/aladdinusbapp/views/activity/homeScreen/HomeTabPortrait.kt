package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
//import com.datalogic.aladdin.aladdinusbapp.utils.CommonUtils.rememberEnsureBluetoothEnabled
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.devicesScreen.DevicesScreenPreview_Populated
import com.datalogic.aladdin.aladdinusbapp.views.compose.BluetoothDeviceDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbapp.views.compose.ConnectionTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.DeviceTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.LabelCodeTypeDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.LabelIDControlDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.LoggingDropdown
import com.datalogic.aladdin.aladdinusbapp.views.compose.RestartDeviceDialog
import com.datalogic.aladdin.aladdinusbapp.views.compose.UsbDeviceDropdown
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun HomeTabPortrait() {

    val homeViewModel = LocalHomeViewModel.current
    val deviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val status = homeViewModel.status.observeAsState(DeviceStatus.NONE).value

    val scanLabel = homeViewModel.scanLabel.observeAsState("").value
    val scanData = homeViewModel.scanData.observeAsState("").value
    val scanRawData = homeViewModel.scanRawData.observeAsState("").value
    val selectedDevice = homeViewModel.selectedDevice.observeAsState(null).value

    val autoDetectChecked = homeViewModel.autoDetectChecked.observeAsState(true).value
    val isBluetoothEnabled = homeViewModel.isBluetoothEnabled.observeAsState(false).value

    val selectedBluetoothDevice = homeViewModel.selectedBluetoothDevice.observeAsState(null).value
    val allBluetoothDevices = homeViewModel.allBluetoothDevices.observeAsState(ArrayList()).value

    val usbDeviceList = homeViewModel.usbDeviceList.observeAsState(ArrayList()).value
    val selectedUsbDevice = homeViewModel.selectedUsbDevice.observeAsState(null).value
    val isEnableScale = homeViewModel.isEnableScale.observeAsState(false).value
    val isScaleAvailable = homeViewModel.isScaleAvailable.observeAsState(false).value

    val context = LocalContext.current
    val activity = context as? Activity
//    val ensureBluetoothEnabled = rememberEnsureBluetoothEnabled(context)
    Column(
        modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen._4sdp))
    ) {
        LoggingDropdown()
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._10sdp)))
        DevicesScreenPreview_Populated()
    }
}
