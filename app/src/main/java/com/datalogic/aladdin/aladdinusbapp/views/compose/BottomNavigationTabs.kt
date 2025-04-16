package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun BottomNavigationRow(modifier: Modifier, homeViewModel: HomeViewModel) {
    val items = listOf(stringResource(id = R.string.home), stringResource(id = R.string.configuration), stringResource(id = R.string.direct_io), stringResource(id = R.string.image_capture))
    val selectedTab by homeViewModel.selectedTabIndex.observeAsState(0)
    val status = homeViewModel.status.observeAsState(DeviceStatus.CLOSED).value
    val selectedDevice = homeViewModel.selectedDevice.observeAsState(null).value

    LaunchedEffect(status) {
        when (status) {
            DeviceStatus.CLOSED,
            DeviceStatus.NONE->
            homeViewModel.setSelectedTabIndex(0) // Switch to home tab
            else -> {}
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen._60sdp))
            .background(
                color = colorResource(id = R.color.colorPrimary)
            )
            .padding(dimensionResource(id = R.dimen._16sdp)),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .clickable {
                        if (homeViewModel.deviceList.value?.size!! > 0) {
                            if (status == DeviceStatus.OPENED) {
                                if ((index == 1 || index == 3) && homeViewModel.selectedDevice.value?.connectionType == ConnectionType.USB_OEM) {
                                    homeViewModel.oemAlert = true
                                } else {
                                    homeViewModel.setSelectedTabIndex(index)
                                    if (index == 1) {
                                        if (selectedDevice?.usbDevice?.productId.toString() == "16386"){
                                            homeViewModel.magellanConfigAlert = true
                                        } else {
                                            homeViewModel.readConfigData()
                                        }
                                    }
                                }
                            } else {
                                if (index != 0) {
                                    homeViewModel.openAlert = true
                                }
                            }
                        } else {
                            if (index != 0) {
                                homeViewModel.connectDeviceAlert = true
                            }
                        }
                    },
                text = item,
                color = if (selectedTab == index) colorResource(id = R.color.bottom_nav_selected_background) else Color.White,
            )
        }
    }
}