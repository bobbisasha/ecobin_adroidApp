package com.example.ecobinapp_v1.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ecobinapp_v1.BinViewModel
import com.example.ecobinapp_v1.model.Bin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinListScreen(
    vm: BinViewModel,
    onAddBin: () -> Unit,
    onOpenBin: (Bin) -> Unit
) {
    val bins by vm.bins.collectAsState()
    var pendingDelete by remember { mutableStateOf<Bin?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("EcoBin") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddBin) { Text("Add bin") }
        }
    ) { padding ->
        if (bins.isEmpty()) {
            EmptyState(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bins, key = { it.id }) { bin ->
                    BinCard(
                        bin = bin,
                        onOpen = { onOpenBin(bin) },
                        onDelete = { pendingDelete = bin }
                    )
                }
            }
        }
    }

    pendingDelete?.let { bin ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove bin?") },
            text = { Text("\"${bin.name}\" will be removed from this app. The device itself is not affected.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteBin(bin.id)
                    pendingDelete = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "No bins yet",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Tap \"Add bin\" to scan for a nearby device and register your first bin.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun BinCard(bin: Bin, onOpen: () -> Unit, onDelete: () -> Unit) {
    val percent = bin.fullnessPercent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FullnessGauge(
                percent = percent,
                diameter = 72.dp,
                strokeWidth = 8.dp,
                labelSize = 16
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bin.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = buildString {
                        append(if (bin.lastDistanceCm != null) "${bin.lastDistanceCm} cm" else "no reading")
                        append(" · depth ${bin.depthCm} cm")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "updated ${formatAgo(bin.lastUpdatedEpochMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onDelete) { Text("Remove") }
        }
    }
}
