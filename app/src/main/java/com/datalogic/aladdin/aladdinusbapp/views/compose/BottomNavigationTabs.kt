package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConnectionType
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus

@Composable
fun BottomNavigationRow(modifier: Modifier, homeViewModel: HomeViewModel) {
    val items = listOf(
        stringResource(id = R.string.home),
        stringResource(id = R.string.configuration),
        stringResource(id = R.string.direct_io),
        stringResource(id = R.string.image_capture),
        stringResource(id = R.string.custom_configuration)
    )
    val selectedTab by homeViewModel.selectedTabIndex.observeAsState(0)
    val status = homeViewModel.status.observeAsState(DeviceStatus.CLOSED).value

    // Screen configuration to calculate available width
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val density = LocalDensity.current.density
    val paddingDp = with(LocalDensity.current) {
        dimensionResource(id = R.dimen._16sdp).toPx().toInt() / density
    }.toInt() * 2  // Account for horizontal padding

    // Determine layout strategy based on screen width
    val availableWidth = screenWidth - paddingDp

    // Fixed approach: For small screens, we'll use a static 3-tab layout with "More" option
    // This ensures we always show complete text for visible tabs
    val useFixedVisibleTabs = availableWidth < 450 // Adjust threshold as needed

    // Always show 3 tabs (Home, Configuration, More) in the main layout if in fixed mode
    val visibleItemCount = if (useFixedVisibleTabs) 3 else items.size
    val showOverflow = items.size > visibleItemCount
    val visibleTabCount = if (showOverflow) visibleItemCount - 1 else items.size

    // State for overflow menu
    val (overflowMenuExpanded, setOverflowMenuExpanded) = remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        when (status) {
            DeviceStatus.CLOSED, DeviceStatus.NONE -> homeViewModel.setSelectedTabIndex(0) // Switch to home tab
            else -> {}
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen._60sdp))
            .background(color = colorResource(id = R.color.colorPrimary))
            .padding(horizontal = dimensionResource(id = R.dimen._8sdp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Changed from SpaceEvenly to SpaceBetween for better alignment
    ) {
        // Display main visible tabs with proper spacing
        items.take(visibleTabCount).forEachIndexed { index, item ->
            NavigationTabItem(
                item = item,
                isSelected = selectedTab == index,
                onTabSelected = {
                    homeViewModel.handleTabSelection(index)
                },
                modifier = Modifier.weight(1f),
                fontSize = 16.sp // Increased font size from 14sp to 16sp
            )
        }

        // Overflow menu if needed
        if (showOverflow) {
            val overflowIndexStart = visibleTabCount
            val overflowItems = items.drop(overflowIndexStart)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentSize(align = Alignment.Center) // Center align instead of right align
            ) {
                NavigationTabItem(
                    item = stringResource(R.string.more), // Use string resource instead of hardcoded "More"
                    isSelected = selectedTab >= overflowIndexStart,
                    onTabSelected = { setOverflowMenuExpanded(true) },
                    fontSize = 16.sp,
                    modifier = Modifier // Removed right alignment to keep consistent with other tabs
                )

                DropdownMenu(
                    expanded = overflowMenuExpanded,
                    onDismissRequest = { setOverflowMenuExpanded(false) },
                    offset = DpOffset(x = 0.dp, y = 0.dp), // Increase negative y-offset to move it lower
                    modifier = Modifier.width(IntrinsicSize.Min)
                ) {
                    overflowItems.forEachIndexed { i, item ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            onClick = {
                                homeViewModel.handleTabSelection(overflowIndexStart + i)
                                setOverflowMenuExpanded(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationTabItem(
    item: String,
    isSelected: Boolean,
    onTabSelected: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp  // Default font size increased to 16sp
) {
    Text(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 8.dp)  // Increased horizontal padding
            .clickable { onTabSelected() },
        text = item,
        color = if (isSelected)
            colorResource(id = R.color.bottom_nav_selected_background)
        else
            Color.White,
        textAlign = TextAlign.Center,
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Visible
    )
}