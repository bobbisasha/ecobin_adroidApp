package com.example.ecobinapp_v1.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.ecobinapp_v1.BinViewModel
import com.example.ecobinapp_v1.R
import com.example.ecobinapp_v1.ble.ConnectionState
import com.example.ecobinapp_v1.model.Bin
import com.example.ecobinapp_v1.ui.theme.Ink
import com.example.ecobinapp_v1.ui.theme.IoTBlue
import com.example.ecobinapp_v1.ui.theme.MonoFamily
import com.example.ecobinapp_v1.ui.theme.Muted
import com.example.ecobinapp_v1.ui.theme.MutedGrey
import com.example.ecobinapp_v1.ui.theme.StatusCritical

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinDetailScreen(
    vm: BinViewModel,
    binId: String,
    onConnect: () -> Unit,
    onBack: () -> Unit
) {
    val bins by vm.bins.collectAsState()
    val bin = bins.firstOrNull { it.id == binId }

    LaunchedEffect(bin == null) {
        if (bin == null) onBack()
    }
    if (bin == null) return

    val liveDistance by vm.ble.distanceCm.collectAsState()
    val connState by vm.ble.connectionState.collectAsState()

    LaunchedEffect(binId) { onConnect() }
    DisposableEffect(binId) { onDispose { vm.disconnect() } }

    val distance = liveDistance ?: bin.lastDistanceCm
    val percent = distance?.let { Bin.fullnessPercent(it, bin.depthCm) }

    var showEdit by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    Scaffold(
        topBar = {
            EcoTopBar(
                title = bin.name,
                onBack = onBack,
                actions = { TextButton(onClick = { showEdit = true }) { Text("Edit") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            FullnessGauge(
                percent = percent,
                diameter = 232.dp,
                strokeWidth = 22.dp,
                labelSize = 54
            )

            val (label, color) = connectionVisual(connState)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_bluetooth),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(text = label, color = color)
            }

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    InfoRow("Distance to trash", distance?.let { "$it cm" } ?: "—")
                    Spacer(Modifier.height(10.dp))
                    InfoRow("Bin depth", "${bin.depthCm} cm")
                    Spacer(Modifier.height(10.dp))
                    InfoRow("Last updated", formatAgo(bin.lastUpdatedEpochMs))
                }
            }

            if (connState == ConnectionState.CONNECTED || connState == ConnectionState.CONNECTING) {
                OutlinedButton(
                    onClick = { vm.disconnect() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Disconnect") }
            } else {
                Button(
                    onClick = onConnect,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reconnect") }
            }
        }
    }

    if (showEdit) {
        EditBinDialog(
            bin = bin,
            onDismiss = { showEdit = false },
            onConfirm = { name, depth ->
                vm.updateBinDetails(bin, name, depth)
                showEdit = false
            }
        )
    }
}

private fun connectionVisual(state: ConnectionState): Pair<String, Color> = when (state) {
    ConnectionState.CONNECTING -> "Connecting…" to IoTBlue
    ConnectionState.CONNECTED -> "Connected" to IoTBlue
    ConnectionState.FAILED -> "Connection failed" to StatusCritical
    ConnectionState.DISCONNECTED -> "Disconnected" to MutedGrey
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Muted)
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontFamily = MonoFamily,
            color = Ink
        )
    }
}

@Composable
private fun EditBinDialog(
    bin: Bin,
    onDismiss: () -> Unit,
    onConfirm: (name: String, depthCm: Int) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue(bin.name)) }
    var depthText by remember { mutableStateOf(TextFieldValue(bin.depthCm.toString())) }
    val depth = depthText.text.toIntOrNull()
    val valid = depth != null && depth in 1..1000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit bin") },
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
                    singleLine = true,
                    isError = depthText.text.isNotEmpty() && !valid,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = { onConfirm(name.text, depth!!) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
