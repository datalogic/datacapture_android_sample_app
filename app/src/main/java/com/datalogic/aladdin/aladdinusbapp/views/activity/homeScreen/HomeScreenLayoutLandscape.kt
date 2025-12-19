package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.activity.checkDockScreen.CheckDockScreenLandscape
import com.datalogic.aladdin.aladdinusbapp.views.activity.checkDockScreen.CheckDockScreenPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.customConfigurationScreen.CustomConfigurationPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.imageCapture.ImageCaptureTabPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.scaleScreen.ScaleScreenPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.scannerScreen.ScannerScreenPortrait
import com.datalogic.aladdin.aladdinusbapp.views.activity.updateFirmware.UpdateFirmwareScreen
import com.datalogic.aladdin.aladdinusbapp.views.compose.BottomNavigationRow
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.FooterImageView
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.HeaderImageView
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.ShowLoading
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.ShowPercentLoading
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.ShowPopup

@Composable
fun HomeScreenLayoutLandscape() {
    val homeViewModel = LocalHomeViewModel.current
    val selectedTab by homeViewModel.selectedTabIndex.observeAsState(0)
    var isLoading = homeViewModel.isLoading.observeAsState().value
    var isLoadingPercent = homeViewModel.isLoadingPercent.observeAsState().value
    var progressUpgrade = homeViewModel.progressUpgrade.observeAsState().value

    val ignoreAlerts = homeViewModel.selectedTabIndex.value != 6 && homeViewModel.selectedTabIndex.value != 7
    ShowPopup((homeViewModel.openAlert && ignoreAlerts), onDismiss = { homeViewModel.openAlert = false }, stringResource(id = R.string.alert_message_for_open_device))
    ShowPopup((homeViewModel.oemAlert && ignoreAlerts), onDismiss = { homeViewModel.oemAlert = false }, stringResource(R.string.oem_device_feature_restriction))
    ShowPopup((homeViewModel.bluetoothAlert && ignoreAlerts), onDismiss = { homeViewModel.bluetoothAlert = false }, stringResource(R.string.not_support_ble_device))
    ShowPopup((homeViewModel.connectDeviceAlert && ignoreAlerts), onDismiss = { homeViewModel.connectDeviceAlert = false }, stringResource(id = R.string.alert_message_for_connect_device))
    ShowPopup((homeViewModel.magellanConfigAlert  && ignoreAlerts), onDismiss = { homeViewModel.magellanConfigAlert = false }, stringResource(id = R.string.alert_message_for_magellan_config))
    ShowPopup((homeViewModel.noDeviceSupportAlert  && ignoreAlerts), onDismiss = { homeViewModel.noDeviceSupportAlert = false }, stringResource(id = R.string.alert_message_for_no_device_support))

    if (isLoading!!) {
        ShowLoading(onDismiss = { isLoading = false })
    }

    if (isLoadingPercent!!) {
        ShowPercentLoading(onDismiss = { isLoading = false }, "$progressUpgrade%")
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        if (selectedTab != 6) {
            HeaderImageView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            )
        }
        Column(
            modifier = Modifier
                .semantics { contentDescription = "home_tab_content_layout" }
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(id = R.dimen._20sdp),
                    vertical = dimensionResource(id = R.dimen._5sdp)
                )
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (selectedTab) {
                0 -> HomeTabPortrait()
                1 ->{
                    ConfigurationTabPortrait()
                }
                2 -> {
                    homeViewModel.setDefaultDevice()
                    DirectIOTabPortrait()
                }
                3 ->{
                    ImageCaptureTabPortrait()
                }
                4 -> {
                    CustomConfigurationPortrait()
                }
                5 ->{
                    UpdateFirmwareScreen()
                }
                6 -> BluetoothTabPortrait()
                7 -> SettingsTabPortrait()
                8 -> ScannerScreenPortrait()
                9 -> ScaleScreenPortrait()
                10 -> CheckDockScreenLandscape()
            }
        }

//        if (selectedTab != 6) {
//            Card(
//                shape = RoundedCornerShape(dimensionResource(id = R.dimen._10sdp)),
//                colors = CardDefaults.cardColors(colorResource(id = R.color.bottom_nav_selected_background)),
//                elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen._10sdp)),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentHeight()
//                    .padding(horizontal = dimensionResource(id = R.dimen._20sdp))
//            ) {
//                Text(
//                    modifier = Modifier
//                        .semantics { contentDescription = "status_msg" }
//                        .fillMaxWidth()
//                        .height(dimensionResource(id = R.dimen._35sdp))
//                        .padding(
//                            vertical = dimensionResource(id = R.dimen._5sdp),
//                            horizontal = dimensionResource(id = R.dimen._15sdp)
//                        ),
//                    text = stringResource(id = R.string.status_label) + deviceStatus,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//        }

        FooterImageView(
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
@Preview
@Composable
fun PreviewHomeScreenLayoutLandscape() {
    HomeScreenLayoutLandscape()
}