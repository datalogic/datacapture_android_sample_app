package com.datalogic.aladdin.aladdinusbapp.views.activity.homeScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomSwitch
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.ConfigurationFeature

@Composable
fun ConfigurationTabLandscape() {
    val homeViewModel = LocalHomeViewModel.current
    val context = LocalContext.current
    val configData = homeViewModel.readConfigData.observeAsState(hashMapOf()).value
    val writeResult = homeViewModel.resultLiveData.observeAsState("").value
    val checkedStates = remember { mutableStateMapOf<ConfigurationFeature, Boolean>() }

    configData.forEach { (feature, value) ->
        if (checkedStates[feature] == null) {
            checkedStates[feature] = value
        }
    }

    if (writeResult.isNotBlank()) {
        Toast.makeText(context, writeResult, Toast.LENGTH_SHORT).show()
        homeViewModel.resultLiveData.value = ""
    }

    val isButtonClicked = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Text(
            modifier = Modifier
                .semantics { contentDescription = "lbl_configuration" }
                .fillMaxWidth()
                .padding(bottom = dimensionResource(id = R.dimen._5sdp)),
            text = stringResource(id = R.string.configuration),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )

        Column (
            modifier = Modifier
                .semantics { contentDescription = "symbology_list" }
                .fillMaxSize()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(
                    colorResource(id = R.color.history_card_view_background),
                    RoundedCornerShape(dimensionResource(id = R.dimen._5sdp))
                )
                .padding(dimensionResource(id = R.dimen._8sdp)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen._30sdp))
        ){
            val sortedConfigData = configData.toList().sortedBy { it.first.featureName }
            for ((feature, value) in sortedConfigData) {
                val checkedState = checkedStates[feature] ?: false
                val oldState = configData[feature] ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimensionResource(id = R.dimen._5sdp)),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = feature.featureName,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    CustomSwitch(
                        checkedState = checkedState,
                        onClick = { newState ->
                            if (checkedState != newState) {
                                checkedStates[feature] = newState
                                homeViewModel.updateWriteConfigData(feature, newState, oldState != newState)
                            }
                            if (isButtonClicked.value) {
                                isButtonClicked.value = false
                            }
                        }
                    )
                }
            }
        }

        Button(
            modifier = Modifier
                .semantics { contentDescription = "apply_button" }
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen._50sdp))
                .padding(top = dimensionResource(id = R.dimen._5sdp)),
            onClick = {
                homeViewModel.applyConfiguration()
                isButtonClicked.value = true
            },
            enabled = homeViewModel.writeConfigData.isNotEmpty() && !isButtonClicked.value,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.colorPrimary),
                disabledContainerColor = colorResource(id = R.color.apply_button_disabled)
            ),
            interactionSource = remember { MutableInteractionSource() }
        ) {
            Text(
                text = stringResource(id = R.string.apply),
                style = MaterialTheme.typography.headlineLarge,
                color = if (homeViewModel.writeConfigData.isNotEmpty() && !isButtonClicked.value) colorResource(id = R.color.white)
                else colorResource(id = R.color.apply_text)
            )
        }
    }
}