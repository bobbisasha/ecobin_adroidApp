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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ecobinapp_v1.BinViewModel
import com.example.ecobinapp_v1.R
import com.example.ecobinapp_v1.model.Bin
import com.example.ecobinapp_v1.ui.theme.Ink
import com.example.ecobinapp_v1.ui.theme.MonoFamily
import com.example.ecobinapp_v1.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BinListScreen(
    vm: BinViewModel,
    onAddBin: () -> Unit,
    onOpenBin: (Bin) -> Unit,
    onAbout: () -> Unit
) {
    val bins by vm.bins.collectAsState()
    var pendingDelete by remember { mutableStateOf<Bin?>(null) }

    Scaffold(
        topBar = {
            EcoTopBar(
                "ecobin",
                actions = { TextButton(onClick = onAbout) { Text("About") } }
            )
        },
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
                    top = padding.calculateTopPadding() + 4.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp
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
            Icon(
                painter = painterResource(R.drawable.ic_bin),
                contentDescription = null,
                tint = Muted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text("No bins yet", style = MaterialTheme.typography.titleLarge, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tap \"Add bin\" to scan for a nearby device and register your first bin.",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BinCard(bin: Bin, onOpen: () -> Unit, onDelete: () -> Unit) {
    val percent = bin.fullnessPercent
    AppCard(
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
                diameter = 76.dp,
                strokeWidth = 9.dp,
                labelSize = 17
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = bin.name, style = MaterialTheme.typography.titleMedium, color = Ink)
                Spacer(Modifier.height(6.dp))
                StatusBadge(text = fullnessStatusLabel(percent), color = fillColor(percent))
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${bin.lastDistanceCm ?: "—"} cm · depth ${bin.depthCm} cm · ${formatAgo(bin.lastUpdatedEpochMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = MonoFamily,
                    color = Muted
                )
            }
            TextButton(onClick = onDelete) { Text("Remove", color = Muted) }
        }
    }
}
