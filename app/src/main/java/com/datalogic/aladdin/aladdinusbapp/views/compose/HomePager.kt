package com.datalogic.aladdin.aladdinusbapp.views.compose


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomePager() {
    val tabs = listOf("Page One", "Page Two")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(title = { Text("Two-Page Pager") })
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        PagerWithLazyLists(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            pagerState = pagerState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagerWithLazyLists(
    modifier: Modifier = Modifier,
    pagerState: PagerState
) {
    val pageOneItems = remember { (1..50).map { "Item #$it (Page 1)" } }
    val pageTwoItems = remember { (1..50).map { "Row #$it (Page 2)" } }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        when (page) {
            0 -> DeviceLis(items = pageOneItems)
            1 -> PageList(items = pageTwoItems)
        }
    }
}