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


import android.content.ContentValues.TAG
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
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButton
import com.datalogic.aladdin.aladdinusbapp.views.compose.ComposableUtils.CustomButtonRow
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
    val devices: List<Device> = emptyList(),
    val query: String = "",
    val filter: DeviceFilter = DeviceFilter.All,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedIds: Set<String> = emptySet(),
) {
    val counts: Triple<Int, Int, Int>
        get() {
            val active = devices.count { it.isActive }
            val inactive = devices.size - active
            return Triple(devices.size, active, inactive)
        }

    val filtered: List<Device>
        get() = devices
            .filter { d ->
                when (filter) {
                    DeviceFilter.All -> true
                    DeviceFilter.Active -> d.isActive
                    DeviceFilter.Inactive -> !d.isActive
                }
            }
            .filter { d ->
                val q = query.trim().lowercase()
                q.isEmpty() || d.name.lowercase().contains(q) || d.model.lowercase().contains(q)
            }

    val selectionMode: Boolean get() = selectedIds.isNotEmpty()
}

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
    state: DevicesUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (DeviceFilter) -> Unit,
    onToggleActive: (Device) -> Unit,
    onRefresh: () -> Unit,
    // NEW callbacks for selection + bulk actions
    onToggleSelect: (Device) -> Unit,
    onEnterSelectionWith: (Device) -> Unit,
    onClearSelection: () -> Unit,
    onBulkActivate: () -> Unit,
    onBulkDeactivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            if (state.selectionMode) {
                SelectionTopBar(
                    selectedCount = state.selectedIds.size,
                    onClose = onClearSelection,
                    onBulkActivate = onBulkActivate,
                    onBulkDeactivate = onBulkDeactivate
                )
            } else {
                MainTopBar(onRefresh = onRefresh)
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Column(
            modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            // Content
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> LoadingState()
                    state.error != null -> ErrorState(state.error, onRetry = onRefresh)
                    state.filtered.isEmpty() -> EmptyState()
                    else -> DevicesList(
                        items = state.filtered,
                        selectionMode = state.selectionMode,
                        selectedIds = state.selectedIds,
                        onToggle = onToggleActive,
                        onToggleSelect = onToggleSelect,
                        onEnterSelectionWith = onEnterSelectionWith
                    )
                }
            }
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

@Composable
private fun SearchBarRow(
    query: String,
    onQueryChange: (String) -> Unit,
    counts: Triple<Int, Int, Int>,
) {
    val (all, active, inactive) = counts
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search by name or model…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CountPill(label = "All", count = all)
            CountPill(label = "Active", count = active)
            CountPill(label = "Inactive", count = inactive)
        }
    }
}

@Composable
private fun CountPill(label: String, count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DevicesList(
    items: List<Device>,
    selectionMode: Boolean,
    selectedIds: Set<String>,
    onToggle: (Device) -> Unit,
    onToggleSelect: (Device) -> Unit,
    onEnterSelectionWith: (Device) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { device ->
            val selected = selectedIds.contains(device.id)
            DeviceRow(
                device = device,
                selectionMode = selectionMode,
                selected = selected,
                onToggle = onToggle,
                onToggleSelect = { onToggleSelect(device) },
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (selectionMode) onToggleSelect(device) else onToggle(device)
                    },
                    onLongClick = { onEnterSelectionWith(device) }
                )
            )
        }
    }
}

@Composable
private fun DeviceRow(
    device: Device,
    selectionMode: Boolean,
    selected: Boolean,
    onToggle: (Device) -> Unit,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onToggleSelect() })
                Spacer(Modifier.width(6.dp))
            } else {
                // Status dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (device.isActive) Color(0xFF34C759) else Color(0xFF999999))
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        append(device.model)
                        device.owner?.let { append(" • ").append(it) }
                        append(" • ")
                        append(if (device.isActive) "Active" else "Inactive")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))
            if (!selectionMode) {
                CustomButtonRow(
                    modifier = Modifier
                        .weight(0.5f)
                        .semantics { contentDescription = "btn_open" },
                    buttonState = true,
                    stringResource(id = R.string.open),
                    onClick = {
                        Log.d(TAG, "btn_open on click")
                        /*activity?.let {
                            homeViewModel.openDevice(activity)
                        }*/
                    }
                )
            }
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

@Composable
fun rememberDevicesController(): StateAndActions {
    val scope = rememberCoroutineScope()
    var state by remember { mutableStateOf(DevicesUiState(isLoading = true)) }

    // Load data once
    LaunchedEffect(Unit) {
        state = state.copy(isLoading = true, error = null)
        runCatching { DevicesRepositoryMock.load() }
            .onSuccess { devices -> state = state.copy(devices = devices, isLoading = false) }
            .onFailure { state = state.copy(isLoading = false, error = it.message ?: "Failed to load") }
    }

    fun toggle(d: Device) {
        // Optimistic update for demo
        state = state.copy(devices = state.devices.map { if (it.id == d.id) it.copy(isActive = !it.isActive) else it })
    }

    fun toggleSelect(d: Device) {
        state = state.copy(selectedIds = state.selectedIds.toMutableSet().apply {
            if (contains(d.id)) remove(d.id) else add(d.id)
        })
    }

    fun enterSelectionWith(d: Device) {
        if (!state.selectedIds.contains(d.id)) {
            state = state.copy(selectedIds = state.selectedIds + d.id)
        }
    }

    fun clearSelection() {
        state = state.copy(selectedIds = emptySet())
    }

    fun bulkActivate() {
        val ids = state.selectedIds
        state = state.copy(
            devices = state.devices.map { if (ids.contains(it.id)) it.copy(isActive = true) else it },
            selectedIds = emptySet()
        )
    }

    fun bulkDeactivate() {
        val ids = state.selectedIds
        state = state.copy(
            devices = state.devices.map { if (ids.contains(it.id)) it.copy(isActive = false) else it },
            selectedIds = emptySet()
        )
    }

    return StateAndActions(
        state = state,
        onQueryChange = { q -> state = state.copy(query = q) },
        onFilterChange = { f -> state = state.copy(filter = f) },
        onToggleActive = { toggle(it) },
        onRefresh = {
            scope.launch {
                state = state.copy(isLoading = true, error = null)
                delay(350)
                runCatching { DevicesRepositoryMock.load() }
                    .onSuccess { devices -> state = state.copy(devices = devices, isLoading = false) }
                    .onFailure { state = state.copy(isLoading = false, error = it.message ?: "Failed to refresh") }
            }
        },
        onToggleSelect = { toggleSelect(it) },
        onEnterSelectionWith = { enterSelectionWith(it) },
        onClearSelection = { clearSelection() },
        onBulkActivate = { bulkActivate() },
        onBulkDeactivate = { bulkDeactivate() },
    )
}

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
    val state = DevicesUiState(
        devices = previewDevices(),
        isLoading = false,
        query = "",
        filter = DeviceFilter.All,
        selectedIds = emptySet(),
    )
    MaterialTheme(colorScheme = lightColorScheme()) {
        DevicesScreen(
            state = state,
            onQueryChange = {},
            onFilterChange = {},
            onToggleActive = {},
            onRefresh = {},
            onToggleSelect = {},
            onEnterSelectionWith = {},
            onClearSelection = {},
            onBulkActivate = {},
            onBulkDeactivate = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DevicesScreenPreview_SelectionMode() {
    val state = DevicesUiState(
        devices = previewDevices(),
        isLoading = false,
        selectedIds = setOf("2", "4"),
    )
    MaterialTheme(colorScheme = lightColorScheme()) {
        DevicesScreen(
            state = state,
            onQueryChange = {},
            onFilterChange = {},
            onToggleActive = {},
            onRefresh = {},
            onToggleSelect = {},
            onEnterSelectionWith = {},
            onClearSelection = {},
            onBulkActivate = {},
            onBulkDeactivate = {},
        )
    }
}

// --------- How to use in your Activity ---------
/*
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val controller = rememberDevicesController()
            MaterialTheme(colorScheme = dynamicLightColorScheme(this)) {
                DevicesScreen(
                    state = controller.state,
                    onQueryChange = controller.onQueryChange,
                    onFilterChange = controller.onFilterChange,
                    onToggleActive = controller.onToggleActive,
                    onRefresh = controller.onRefresh,
                    onToggleSelect = controller.onToggleSelect,
                    onEnterSelectionWith = controller.onEnterSelectionWith,
                    onClearSelection = controller.onClearSelection,
                    onBulkActivate = controller.onBulkActivate,
                    onBulkDeactivate = controller.onBulkDeactivate,
                )
            }
        }
    }
}
*/


