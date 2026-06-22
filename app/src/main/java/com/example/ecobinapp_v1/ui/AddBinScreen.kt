package com.example.ecobinapp_v1.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.ecobinapp_v1.BinViewModel
import com.example.ecobinapp_v1.ble.DiscoveredDevice
import com.example.ecobinapp_v1.model.Bin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBinScreen(
    vm: BinViewModel,
    onScanRequested: () -> Unit,
    onRegistered: (Bin) -> Unit,
    onBack: () -> Unit
) {
    val isScanning by vm.ble.isScanning.collectAsState()
    val devices by vm.ble.discovered.collectAsState()
    val demoMode by vm.ble.demoMode.collectAsState()
    val status by vm.ble.status.collectAsState()

    var selected by remember { mutableStateOf<DiscoveredDevice?>(null) }

    fun leave() {
        vm.stopScan()
        onBack()
    }
    BackHandler { leave() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a bin") },
                navigationIcon = {
                    TextButton(onClick = { leave() }) { Text("Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Demo mode", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Simulates a bin so you can test without hardware (required on the emulator).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(checked = demoMode, onCheckedChange = { vm.setDemoMode(it) })
                    }
                }
            }

            item {
                Button(
                    onClick = onScanRequested,
                    enabled = !isScanning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Scanning…")
                    } else {
                        Text("Scan for devices")
                    }
                }
            }

            if (status.isNotBlank()) {
                item {
                    Text(
                        status,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(devices, key = { it.address }) { device ->
                DeviceRow(device = device, onClick = { selected = device })
            }

            if (devices.isEmpty() && !isScanning) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No devices yet. Make sure the bin device is powered on and nearby, then scan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    selected?.let { device ->
        RegisterDialog(
            device = device,
            onDismiss = { selected = null },
            onConfirm = { name, depth ->
                val bin = vm.registerBin(name, depth, device)
                selected = null
                onRegistered(bin)
            }
        )
    }
}

@Composable
private fun DeviceRow(device: DiscoveredDevice, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(device.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("${device.rssi} dBm", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RegisterDialog(
    device: DiscoveredDevice,
    onDismiss: () -> Unit,
    onConfirm: (name: String, depthCm: Int) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(device.name ?: "")) }
    var depthText by remember { mutableStateOf(TextFieldValue("")) }
    val depth = depthText.text.toIntOrNull()
    val valid = depth != null && depth in 1..1000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register bin") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bin name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = depthText,
                    onValueChange = { depthText = it },
                    label = { Text("Bin depth (cm)") },
                    supportingText = { Text("Distance from the sensor to the empty bottom.") },
                    singleLine = true,
                    isError = depthText.text.isNotEmpty() && !valid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = valid,
                onClick = { onConfirm(name.text, depth!!) }
            ) { Text("Register") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
