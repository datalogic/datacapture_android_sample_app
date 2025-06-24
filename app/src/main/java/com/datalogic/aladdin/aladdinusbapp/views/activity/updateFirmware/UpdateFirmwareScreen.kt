package com.datalogic.aladdin.aladdinusbapp.views.activity.updateFirmware

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.FileUtils
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import java.io.File

@Preview(showBackground = true)
@Composable
fun UpdateFirmwareScreen() {
    val homeViewModel = LocalHomeViewModel.current
    val isUpgrade = remember { mutableStateOf(false) }
    val isLoadFile = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val progress = homeViewModel.progressUpgrade.observeAsState().value ?: 0
    var checkPid by remember { mutableStateOf(true) }
    var bulkTransfer by remember { mutableStateOf(false) }
    var file: File? = null

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                file = FileUtils.getFileFromUri(context, it)
                isLoadFile.value = true
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // FW information
            if(isLoadFile.value) {
                ReleaseInformationCard()
                Spacer(modifier = Modifier.height(4.dp))
                UpgradeConfigurationCard(
                    checkPidEnabled = checkPid,
                    bulkTransferEnabled = bulkTransfer,
                    onCheckPidToggle = { checkPid = it },
                    onBulkTransferToggle = { bulkTransfer = it }
                )
            }
            // Progress Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                if (isUpgrade.value) {
                    Text(
                        text = if (progress.toFloat() >= 100) stringResource(R.string.txt_done) else "$progress%",
                        fontSize = 14.sp,
                        color = colorResource(id = R.color.colorPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // Placeholder when not updating
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ready",
                            fontSize = 14.sp,
                            color = colorResource(id = R.color.colorPrimary)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Buttons at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min), // <-- Ensures children align to tallest one
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = { filePickerLauncher.launch("*/*") },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.btn_load_file),
                        color = colorResource(R.color.white),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(8.dp)) // better than weight for spacing

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(), // <-- makes this button match height
                    onClick = {
                        file?.let {
                            homeViewModel.upgradeFirmware(it)
                        }
                        isUpgrade.value = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.btn_upgrade_firmware),
                        color = colorResource(R.color.white),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
} 