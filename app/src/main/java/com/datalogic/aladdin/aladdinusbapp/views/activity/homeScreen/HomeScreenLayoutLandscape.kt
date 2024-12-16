package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.BottomNavigationRow
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.ShowLoading
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.ShowPopup
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun HomeScreenLayoutLandscape() {
    val homeViewModel = LocalHomeViewModel.current

    val deviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val status = homeViewModel.status.observeAsState().value
    val deviceStatus = homeViewModel.deviceStatus.observeAsState("").value
    val selectedTab by homeViewModel.selectedTabIndex.observeAsState(0)
    var isLoading = homeViewModel.isLoading.observeAsState().value

    if (isLoading!!) { ShowLoading(onDismiss = {isLoading = false})}

    ShowPopup(homeViewModel.enableAlert, onDismiss = { homeViewModel.enableAlert = false }, stringResource(id = R.string.alert_message_for_enable_device))
    ShowPopup(homeViewModel.oemAlert, onDismiss = { homeViewModel.oemAlert = false }, stringResource(id = R.string.alert_message_for_oem_configuration))
    ShowPopup(homeViewModel.connectDeviceAlert, onDismiss = { homeViewModel.connectDeviceAlert = false }, stringResource(id = R.string.alert_message_for_connect_device))

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ComposableUtils.HeaderImageView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight()
            ) {
                when (selectedTab) {
                    0 -> HomeTabLandscape()
                    1 -> ConfigurationTabLandscape()
                    2 -> DirectIOTabLandscape()
                }
            }

            Column(
                modifier = Modifier
                    .semantics { contentDescription = "button_layout" }
                    .fillMaxSize()
                    .padding(
                        start = dimensionResource(id = R.dimen._35sdp),
                        end = dimensionResource(id = R.dimen._35sdp),
                        top = dimensionResource(id = R.dimen._10sdp),
                        bottom = dimensionResource(id = R.dimen._10sdp)
                    ),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    modifier = Modifier
                        .semantics { contentDescription = "device_image" }
                        .size(
                            dimensionResource(id = R.dimen._160sdp),
                            dimensionResource(id = R.dimen._100sdp)
                        ),
                    painter = painterResource(id = R.drawable.magellan),
                    contentDescription = "",
                    contentScale = ContentScale.FillBounds
                )
                CustomButton(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_open" }
                        .fillMaxWidth(),
                    buttonState = (status == DeviceStatus.CLOSED && deviceList.isNotEmpty()),
                    stringResource(id = R.string.open),
                    onClick = {
                        homeViewModel.openUsbConnection()
                    }
                )
                CustomButton(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_claim" }
                        .fillMaxWidth(),
                    buttonState = (status == DeviceStatus.OPENED || status == DeviceStatus.RELEASED),
                    stringResource(id = R.string.claim),
                    onClick = {
                        homeViewModel.claim()
                    }
                )
                CustomButton(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_enable" }
                        .fillMaxWidth(),
                    buttonState = (status == DeviceStatus.CLAIMED || status == DeviceStatus.DISABLE),
                    stringResource(id = R.string.enable),
                    onClick = {
                        homeViewModel.enabled()
                    }
                )
                CustomButton(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_disable" }
                        .fillMaxWidth(),
                    buttonState = (status == DeviceStatus.ENABLED),
                    stringResource(id = R.string.disable),
                    onClick = {
                        homeViewModel.disable()
                    }
                )
                CustomButton(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_release" }
                        .fillMaxWidth(),
                    buttonState = (status == DeviceStatus.CLAIMED || status == DeviceStatus.DISABLE),
                    stringResource(id = R.string.release),
                    onClick = {
                        homeViewModel.release()
                    }
                )
                CustomButton(
                    modifier = Modifier
                        .semantics { contentDescription = "btn_close" }
                        .fillMaxWidth(),
                    buttonState = (status == DeviceStatus.OPENED || status == DeviceStatus.RELEASED),
                    stringResource(id = R.string.close),
                    onClick = {
                        homeViewModel.close()
                    }
                )
            }
        }

        Card(
            shape = RoundedCornerShape(dimensionResource(id = R.dimen._10sdp)),
            colors = CardDefaults.cardColors(colorResource(id = R.color.bottom_nav_selected_background)),
            elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen._10sdp)),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = dimensionResource(id = R.dimen._35sdp))
        ) {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "status_msg" }
                    .fillMaxWidth()
                    .padding(
                        vertical = dimensionResource(id = R.dimen._5sdp),
                        horizontal = dimensionResource(id = R.dimen._15sdp)
                    ),
                text = stringResource(id = R.string.status_label) + deviceStatus
            )
        }

        ComposableUtils.FooterImageView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )

        BottomNavigationRow(
            modifier = Modifier
                .semantics { contentDescription = "bottom_nav" }
                .fillMaxWidth()
                .wrapContentHeight(),
            homeViewModel
        )
    }
}