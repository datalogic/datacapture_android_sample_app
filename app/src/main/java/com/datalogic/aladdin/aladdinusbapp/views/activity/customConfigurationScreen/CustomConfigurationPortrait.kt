package com.datalogic.aladdin.aladdinusbapp.views.activity.customConfigurationScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel

@Preview
@Composable
fun CustomConfigurationPortrait() {
    val homeViewModel = LocalHomeViewModel.current
    val configData = homeViewModel.customConfiguration.observeAsState().value
    val textState = remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {

            TextField(
                value = configData ?: "",
                onValueChange = { textState.value = it },
                modifier = Modifier.fillMaxWidth().height(300.dp),
            )

            Spacer(modifier = Modifier.weight(1f)) // Push buttons to bottom

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(onClick = { homeViewModel.readCustomConfig() }) {
                    Text(stringResource(R.string.btn_read))
                }

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._30sdp)))

                Button(onClick = { /* Handle write configuration */ }) {
                    Text(stringResource(R.string.btn_write))
                }
            }
        }
    }
}