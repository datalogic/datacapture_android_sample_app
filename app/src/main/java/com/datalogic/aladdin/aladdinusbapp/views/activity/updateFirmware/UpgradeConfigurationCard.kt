package com.datalogic.aladdin.aladdinusbapp.views.activity.updateFirmware

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun UpgradeConfigurationCard(
    checkPidEnabled: Boolean,
    bulkTransferEnabled: Boolean,
    onCheckPidToggle: (Boolean) -> Unit,
    onBulkTransferToggle: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF1A2B46)
                )
                Text(
                    text = "Upgrade Configuration",
                    color = Color(0xFF1A2B46),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Expand",
                    tint = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            ToggleRow(label = "Check PID", checked = checkPidEnabled, onCheckedChange = onCheckPidToggle)
            ToggleRow(label = "Bulk Transfer", checked = bulkTransferEnabled, onCheckedChange = onBulkTransferToggle)
        }
    }
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        IconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Checked",
                    tint = Color.Green
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "UnChecked",
                    tint = Color.Gray
                )
            }
        }
    }
}
