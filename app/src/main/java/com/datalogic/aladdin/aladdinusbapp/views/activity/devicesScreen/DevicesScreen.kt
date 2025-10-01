package com.datalogic.aladdin.aladdinusbapp.views.activity.devicesScreen

// SPDX-License-Identifier: MIT
// File: DevicesScreen.kt
// A clean, Compose Material 3 UI to view "Active" vs "Inactive" devices and toggle status.
// Drop this into your app module and wire the screen in your NavHost or Activity.


// SPDX-License-Identifier: MIT
// File: DevicesScreen.kt
// A Compose Material 3 UI to view and manage devices with Active/Inactive status.
// NEW: Multi-select with bulk Activate/Deactivate, long-press to enter selection mode,
// per-item selection checkboxes, and quick actions in the TopAppBar.

// SPDX-License-Identifier: MIT
// File: DevicesScreen.kt
// A Compose Material 3 UI to view and manage devices with Active/Inactive status.
// NEW: Multi-select with bulk Activate/Deactivate, long-press to enter selection mode,
// per-item selection checkboxes, and quick actions in the TopAppBar.


import DatalogicBluetoothDevice
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.ContentValues.TAG
import android.hardware.usb.UsbDevice
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.datalogic.aladdin.aladdinusbapp.R
import com.datalogic.aladdin.aladdinusbapp.views.activity.LocalHomeViewModel
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButtonRow
import com.datalogic.aladdin.aladdinusbscannersdk.model.DatalogicDevice
import com.datalogic.aladdin.aladdinusbscannersdk.utils.enums.DeviceStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

// --------- Data layer (sample) ---------

data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val model: String,
    val isActive: Boolean,
    val owner: String? = null,
)

enum class DeviceFilter { All, Active, Inactive }

@Immutable
data class DevicesUiState(
    val devices: List<DatalogicDevice> = emptyList(),
    val query: String = "",
    val filter: DeviceFilter = DeviceFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIds: Set<String> = emptySet(),
)

// Simulated repository for demo purposes.
object DevicesRepositoryMock {
    private val seed = listOf(
        Device(name = "Pixel 8 Pro", model = "Google", isActive = true, owner = "Nora"),
        Device(name = "Galaxy S22", model = "Samsung", isActive = false, owner = "Lee"),
        Device(name = "Nothing Phone (2)", model = "Nothing", isActive = true, owner = "Sam"),
        Device(name = "Xperia 5", model = "Sony", isActive = false),
        Device(name = "Redmi Note 13", model = "Xiaomi", isActive = true),
        Device(name = "Moto G Power", model = "Motorola", isActive = false),
    )

    suspend fun load(): List<Device> {
        delay(600) // simulate network
        return seed
    }
}

// --------- UI layer ---------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    usbDeviceList: ArrayList<DatalogicDevice>,
    bluetoothDeviceList: ArrayList<DatalogicBluetoothDevice>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            MainTopBar(onRefresh = onRefresh)
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Column(modifier.padding(inner).fillMaxSize()) {
            UsbDevicesList(
                items = usbDeviceList,
            )
            BluetoothDevicesList(
                items = bluetoothDeviceList
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(onRefresh: () -> Unit) {
    TopAppBar(
        title = { Text("Devices", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onBulkActivate: () -> Unit,
    onBulkDeactivate: () -> Unit,
) {
    TopAppBar(
        title = { Text("$selectedCount selected", fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close selection") }
        },
        actions = {
            IconButton(onClick = onBulkDeactivate) { Icon(Icons.Default.Call, contentDescription = "Deactivate") }
            IconButton(onClick = onBulkActivate) { Icon(Icons.Default.CheckCircle, contentDescription = "Activate") }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsbDevicesList(
    items: List<DatalogicDevice>
) {
    LazyColumn(
        modifier = Modifier.wrapContentHeight(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { device ->
            UsbDeviceRow(
                device = device
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BluetoothDevicesList(
    items: List<DatalogicBluetoothDevice>
) {
    LazyColumn(
        modifier = Modifier.wrapContentHeight(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { device ->
            BluetoothDeviceRow(
                device = device,
                modifier = Modifier.combinedClickable(
                    onClick = {
                    },
                )
            )
        }
    }
}

@Composable
private fun UsbDeviceRow(
    device: DatalogicDevice,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val homeViewModel = LocalHomeViewModel.current
    val isOpen = device.status.value == DeviceStatus.OPENED
    val buttonText = if (isOpen) {
        stringResource(id = R.string.close)
    } else {
        stringResource(id = R.string.open)
    }
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (device.status.value == DeviceStatus.OPENED) Color(0xFF34C759) else Color(
                            0xFF999999
                        )
                    )
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = device.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.usb_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))
            CustomButtonRow(
                modifier = Modifier
                    .weight(0.5f)
                    .semantics { contentDescription = "btn_open" },
                openState = isOpen,
                name = buttonText,
                onClick = {
                    activity?.let {
                        if(isOpen) {
                            Log.d(TAG, "btn_close on click")
                            homeViewModel.closeUsbDevice(device)
                        } else {
                            Log.d(TAG, "btn_open on click")
                            homeViewModel.openUsbDevice(activity, device)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BluetoothDeviceRow(
    device: DatalogicBluetoothDevice,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val homeViewModel = LocalHomeViewModel.current
    Surface(
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        //if (device. == DeviceStatus.OPENED) Color(0xFF34C759) else
                            Color(
                            0xFF999999
                        )
                    )
            )
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = device.id,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.bluetooth_device),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))
            CustomButtonRow(
                modifier = Modifier
                    .weight(0.5f)
                    .semantics { contentDescription = "btn_open" },
                openState = true,
                stringResource(id = R.string.open),
                onClick = {
                    Log.d(TAG, "btn_open on click")
                    activity?.let {
                        homeViewModel.openBluetoothDevice(activity, device)
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Loading devices…", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No devices match your filters", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// --------- Simple screen state holder (demo) ---------

@Stable
data class StateAndActions(
    val state: DevicesUiState,
    val onQueryChange: (String) -> Unit,
    val onFilterChange: (DeviceFilter) -> Unit,
    val onToggleActive: (Device) -> Unit,
    val onRefresh: () -> Unit,
    val onToggleSelect: (Device) -> Unit,
    val onEnterSelectionWith: (Device) -> Unit,
    val onClearSelection: () -> Unit,
    val onBulkActivate: () -> Unit,
    val onBulkDeactivate: () -> Unit,
)

// NEW: Previews with a static device list (no async), useful for Android Studio Preview
private fun previewDevices() = listOf(
    Device(id = "1", name = "Pixel 8 Pro", model = "Google", isActive = true, owner = "Nora"),
    Device(id = "2", name = "Galaxy S22", model = "Samsung", isActive = false, owner = "Lee"),
    Device(id = "3", name = "Nothing Phone (2)", model = "Nothing", isActive = true, owner = "Sam"),
    Device(id = "4", name = "Xperia 5", model = "Sony", isActive = false),
    Device(id = "5", name = "Redmi Note 13", model = "Xiaomi", isActive = true),
    Device(id = "6", name = "Moto G Power", model = "Motorola", isActive = false),
)

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview_Populated() {
    val homeViewModel = LocalHomeViewModel.current
    val allBluetoothDevices = homeViewModel.selectedScannerBluetoothDevice.observeAsState(ArrayList()).value
    val usbDeviceList = homeViewModel.deviceList.observeAsState(ArrayList()).value

    MaterialTheme(colorScheme = lightColorScheme()) {
        DevicesScreen(
            usbDeviceList = usbDeviceList,
            bluetoothDeviceList = allBluetoothDevices,
            onRefresh = {},
        )
    }
}


