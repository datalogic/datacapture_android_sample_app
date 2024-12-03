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

@Composable
fun BottomNavigationRow(modifier: Modifier, homeViewModel: HomeViewModel) {
    val items = listOf(stringResource(id = R.string.home), stringResource(id = R.string.configuration), stringResource(id = R.string.direct_io))
    val selectedItem by homeViewModel.selectedTabIndex.observeAsState(0)

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
                        homeViewModel.setSelectedTabIndex(index)
                    },
                text = item,
                color = if (selectedItem == index) colorResource(id = R.color.bottom_nav_selected_background) else Color.White,
            )
        }
    }
}