package com.datalogic.aladdin.aladdinusbapp.views.activity.updateFirmware

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.utils.FileUtils
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import java.io.File

@Composable
fun UpdateFirmwareScreen() {
    val homeViewModel = LocalHomeViewModel.current
    val isUpgrade = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val progress = homeViewModel.progressUpgrade.observeAsState().value ?: 0
    val isCompleteUpgrade = homeViewModel.isCompleteUpgrade.observeAsState().value ?: 0
    var file: File? = null

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                file = FileUtils.getFileFromUri(context, it)
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = stringResource(R.string.update_firmware),
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(2f))
            
            // Progress Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                if (isUpgrade.value) {
                    Text(
                        text = if (progress.toFloat() >= 100) stringResource(R.string.txt_done) else "$progress%",
                        fontSize = 20.sp,
                        color = colorResource(id = R.color.colorPrimary)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    // Placeholder when not updating
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Ready",
                            fontSize = 16.sp,
                            color = colorResource(id = R.color.colorPrimary)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Buttons at bottom
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { filePickerLauncher.launch("*/*") },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.btn_load_file),
                        color = colorResource(R.color.white)
                    )
                }
                
                Spacer(modifier = Modifier.weight(0.1f))
                
                Button(
                    modifier = Modifier.weight(1f),
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
                        color = colorResource(R.color.white)
                    )
                }
            }
        }
    }
} 