package com.datalogic.aladdin.aladdinusbapp.views.activity.updateFirmware

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.delay
import java.io.File
import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import com.datalogic.aladdin.aladdinusbapp.viewmodel.HomeViewModel

@Preview(showBackground = true)
@Composable
fun UpdateFirmwareScreen() {
    val homeViewModel = LocalHomeViewModel.current
    val isLoadFile = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val checkPid by homeViewModel.isCheckPid.collectAsState()
    var isCheckPidToggle by remember { mutableStateOf(true) }
    var isBulkTransferToggle by remember { mutableStateOf(false) }
    var file by remember { mutableStateOf<File?>(null) }
    var swName by remember { mutableStateOf("") }
    var filePath by remember { mutableStateOf("") }
    var pid by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                swName = FileUtils.getFileNameFromUri(context, it)
                    .toString().replace(".S37", "")
                file = FileUtils.getFileFromUri(context, it)
                isLoadFile.value = true
                pid = homeViewModel.getPid(file).toString()
                filePath = file?.absolutePath ?: ""
                val realPath = FileUtils.getRealPathFromUri(context, it)
                if (realPath != null) {
                    val file1 = File(realPath)
                    filePath = file1.parent?.toString() ?: ""
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        homeViewModel._isBulkTransferSupported.value = false
    }

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
                ReleaseInformationCard(swName, pid, filePath)
                Spacer(modifier = Modifier.height(4.dp))
                UpgradeConfigurationCard(
                    checkPidEnabled = isCheckPidToggle,
                    bulkTransferEnabled = isBulkTransferToggle,
                    onCheckPidToggle = {
                        isCheckPidToggle = it
                    },
                    onBulkTransferToggle = {
                        isBulkTransferToggle = it
                        if (!isBulkTransferToggle)
                            homeViewModel._isBulkTransferSupported.postValue(false)
                    },
                    isFRS = homeViewModel.isFRS()
                )
            }
            // Progress Section
            
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
                    onClick = {
                        filePickerLauncher.launch("application/octet-stream") },
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.btn_load_file),
                        color = colorResource(R.color.white),
                        fontSize = 14.sp,
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
                            if (isCheckPidToggle) {
                                homeViewModel.setPid(it) { isValid ->
                                    if (isCheckPidToggle != isValid) {
                                        Toast.makeText(context, context.getString(R.string.pid_is_not_valid), Toast.LENGTH_LONG).show()
                                        return@setPid
                                    }
                                    handleBulkTransferAndUpgrade(it, isBulkTransferToggle, homeViewModel, context)
                                }
                                return@let
                            }
                            handleBulkTransferAndUpgrade(it, isBulkTransferToggle, homeViewModel, context)
                        }
                    },
                    enabled = isLoadFile.value,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(id = R.color.colorPrimary),
                        disabledContainerColor = colorResource(id = R.color.colorPrimary).copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        stringResource(R.string.btn_upgrade_firmware),
                        color = colorResource(R.color.white),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

fun handleBulkTransferAndUpgrade(
    file: File,
    isBulkTransferToggle: Boolean,
    homeViewModel: HomeViewModel,
    context: Context
) {
    if (isBulkTransferToggle) {
        homeViewModel.getBulkTransferSupported(file) { supported ->
            if (isBulkTransferToggle != supported) {
                Toast.makeText(
                    context,
                    context.getString(R.string.bulk_transfer_is_not_valid),
                    Toast.LENGTH_LONG
                ).show()
                return@getBulkTransferSupported
            } else {
                homeViewModel.upgradeFirmware(file)
                return@getBulkTransferSupported
            }
        }
    } else {
        homeViewModel.upgradeFirmware(file)
    }
}