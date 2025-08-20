package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel


@Composable
fun BluetoothTabPortrait() {
    val homeViewModel = LocalHomeViewModel.current

    val qrBitmap by homeViewModel.qrBitmap.observeAsState()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val context = LocalContext.current
    val activity = context as? Activity

    /**
     * Define a threshold for vertical scrolling
     * */
    val scrollableThreshold = 500

    activity?.let {
        homeViewModel.scanBluetoothDevice(it)
    }
    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
//            Box(
//                modifier = Modifier
//                    .size(90.dp)
//                    .background(Color.Gray),
//            )
//            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.scan_to_pair),
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(210.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .background(Color.LightGray)
                )
            }
        }
    }

    /**
     * Vertical scroll only if the screen height is less than the threshold
     */
    if (screenHeight < scrollableThreshold) {
        Column(modifier = Modifier
            .padding(vertical = dimensionResource(id = R.dimen._3sdp))
            .verticalScroll(rememberScrollState())) {
            content()
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}