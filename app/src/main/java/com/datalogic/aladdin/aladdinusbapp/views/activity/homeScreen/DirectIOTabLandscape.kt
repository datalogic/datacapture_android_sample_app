package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.DIODropdown
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DIOCmdValue

@Composable
fun DirectIOTabLandscape() {
    val homeViewModel = LocalHomeViewModel.current
    val deviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value
    val selectedCommand = homeViewModel.selectedCommand.observeAsState(DIOCmdValue.IDENTIFICATION).value
    val selectedDevice = homeViewModel.selectedDevice.observeAsState(null).value
    val dioData = homeViewModel.dioData.observeAsState("").value
    val dioStatus = homeViewModel.dioStatus.observeAsState("").value
    val status = homeViewModel.status.observeAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "direct_i/o_tab_content_layout" }
            .padding(
                start = dimensionResource(id = R.dimen._35sdp),
                top = dimensionResource(id = R.dimen._20sdp),
            )
    ) {
        Column {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_command" }
                    .fillMaxWidth()
                    .padding(
                        bottom = dimensionResource(id = R.dimen._5sdp)
                    ),
                text = stringResource(id = R.string.command),
                style = MaterialTheme.typography.headlineLarge
            )

            DIODropdown(
                modifier = Modifier
                    .semantics { contentDescription = "device_list_dropdown" }
                    .fillMaxWidth(),
                selectedCommand = selectedCommand,
                onCommandSelected = { command ->
                    homeViewModel.updateSelectedDIOCommand(command)
                }
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._20sdp)))

        Column(
            modifier = Modifier
                .semantics { contentDescription = "direct_i/o" }
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen._10sdp))
        ) {
            Text(
                modifier = Modifier
                    .semantics { contentDescription = "lbl_direct_i/o" }
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen._5sdp)),
                text = stringResource(id = R.string.direct_io),
                style = MaterialTheme.typography.headlineLarge
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(dimensionResource(id = R.dimen._8sdp)))
                    .padding(dimensionResource(id = R.dimen._16sdp)),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_data" }
                        .fillMaxWidth()
                        .padding(bottom = dimensionResource(id = R.dimen._5sdp)),
                    text = stringResource(id = R.string.data),
                    style = MaterialTheme.typography.labelLarge
                )
                TextField(
                    value = dioData,
                    onValueChange = {
                        homeViewModel.updateDIODataField(it)
                    },
                    modifier = Modifier
                        .semantics { contentDescription = "command_data" }
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(dimensionResource(id = R.dimen._5sdp))),
                    singleLine = true,
                    enabled = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        disabledIndicatorColor = Color.White
                    ),
                )
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "lbl_status_message" }
                        .fillMaxWidth()
                        .padding(
                            top = dimensionResource(id = R.dimen._15sdp),
                            bottom = dimensionResource(id = R.dimen._5sdp)
                        ),
                    text = stringResource(id = R.string.status_message),
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    modifier = Modifier
                        .semantics { contentDescription = "status_message" }
                        .fillMaxWidth()
                        .weight(1f)
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(dimensionResource(id = R.dimen._5sdp)))
                        .padding(dimensionResource(id = R.dimen._8sdp))
                        .verticalScroll(rememberScrollState()),
                    text = dioStatus
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Button(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_execute" }
                            .padding(top = dimensionResource(id = R.dimen._16sdp))
                            .wrapContentSize(),
                        enabled = true,
                        onClick = { homeViewModel.executeDIOCommand() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.colorPrimary),
                            disabledContainerColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(disabledElevation = dimensionResource(id = R.dimen._10sdp)),
                    ) {
                        Text(
                            text = stringResource(id = R.string.execute_dio),
                            style = MaterialTheme.typography.labelLarge,
                            color = colorResource(id = R.color.white)
                        )
                    }

                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._25sdp)))

                    Button(
                        modifier = Modifier
                            .semantics { contentDescription = "btn_clear_status_message" }
                            .padding(top = dimensionResource(id = R.dimen._16sdp))
                            .wrapContentSize(),
                        onClick = {
                            homeViewModel.clearDIOStatus()
                        },
                        colors = ButtonDefaults.buttonColors(colorResource(id = R.color.colorPrimary))
                    ) {
                        Text(
                            text = stringResource(id = R.string.clear_fields),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}