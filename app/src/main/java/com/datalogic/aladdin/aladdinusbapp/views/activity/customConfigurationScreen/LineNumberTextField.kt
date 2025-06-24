package com.datalogic.aladdin.aladdinusbapp.views.activity.customConfigurationScreen

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LineNumberedTextField(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Row(
        modifier = modifier
            .height(300.dp)
            .border(1.dp, Color.Gray)
    ) {
        // Line numbers
        val lines = text.lines().size.coerceAtLeast(1)
        Column(
            modifier = Modifier
                .padding(start = 2.dp, end = 3.dp, top = 3.dp)
                .verticalScroll(verticalScroll)
        ) {
            for (i in 1..lines) {
                Text(
                    text = "$i",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        // TextField in a horizontally scrollable Box
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(verticalScroll)
                .horizontalScroll(horizontalScroll)
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color.Black
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .fillMaxHeight()
                    .padding(4.dp),
                decorationBox = { innerTextField ->
                    innerTextField()
                }
            )
        }
    }
}


