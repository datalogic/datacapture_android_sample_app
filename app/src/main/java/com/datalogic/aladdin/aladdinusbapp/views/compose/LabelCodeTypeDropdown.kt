package com.datalogic.aladdin.aladdinusbapp.views.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.toSize
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbscannersdk.model.LabelCodeType

@Composable
fun LabelCodeTypeDropdown(
    modifier: Modifier = Modifier,
    selectedLabelCodeType: LabelCodeType?,
    onLabelCodeTypeSelected: (LabelCodeType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }
    val labelCodeTypes = LabelCodeType.values()

    val icon = if (expanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Card(
        shape = RoundedCornerShape(dimensionResource(id = R.dimen._5sdp)),
        elevation = CardDefaults.cardElevation(defaultElevation = dimensionResource(id = R.dimen._10sdp)),
        colors = CardDefaults.cardColors(Color.White),
        modifier = modifier
    ) {
        TextField(
            value = "${selectedLabelCodeType?.code} - ${getDescriptionForLabelCodeType(selectedLabelCodeType)} ",
            textStyle = MaterialTheme.typography.labelLarge,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .clickable { expanded = !expanded },
            trailingIcon = {
                Icon(icon, "arrow_dropdown", tint = Color.Black)
            },
            enabled = false,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(color = Color.White)
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
        ) {
            labelCodeTypes.forEach { labelCodeType ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${labelCodeType.code} - ${getDescriptionForLabelCodeType(labelCodeType)}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    onClick = {
                        expanded = false
                        onLabelCodeTypeSelected(labelCodeType)
                    }
                )
            }
        }
    }
}

@Composable
private fun getDescriptionForLabelCodeType(labelCodeType: LabelCodeType?): String {
    return when (labelCodeType) {
        LabelCodeType.USA -> "USA Standard Codes"
        LabelCodeType.EU -> "European Standard Codes"
        LabelCodeType.COMSC -> "COM-SC/RS232 Codes"
        LabelCodeType.NONE -> "No Label Code Type"
        null -> "Select Label Code Type"
    }
}
