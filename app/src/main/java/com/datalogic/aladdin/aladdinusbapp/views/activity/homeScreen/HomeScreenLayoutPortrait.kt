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
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.BottomNavigationRow
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils

@Composable
fun HomeScreenLayoutPortrait() {
    val homeViewModel = LocalHomeViewModel.current
    val deviceStatus = homeViewModel.deviceStatus.observeAsState("").value
    val selectedTab by homeViewModel.selectedTabIndex.observeAsState(0)

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
        Column(
            modifier = Modifier
                .semantics { contentDescription = "home_tab_content_layout" }
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(id = R.dimen._20sdp),
                    vertical = dimensionResource(id = R.dimen._20sdp)
                )
                .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            when (selectedTab) {
                0 -> HomeTabPortrait()
                1 -> ConfigurationTab()
                2 -> DirectIOTab()
            }
        }

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(colorResource(id = R.color.bottom_nav_selected_background)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = dimensionResource(id = R.dimen._20sdp))
        ) {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "status_msg"}
                    .fillMaxWidth()
                    .height(dimensionResource(id = R.dimen._35sdp))
                    .padding(vertical = dimensionResource(id = R.dimen._5sdp), horizontal = dimensionResource(id = R.dimen._15sdp)),
                text = stringResource(id = R.string.status_label) + deviceStatus,
                overflow = TextOverflow.Ellipsis
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