package com.example.ecobinapp_v1.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ecobinapp_v1.ui.theme.EcoGreen
import com.example.ecobinapp_v1.ui.theme.Ink
import com.example.ecobinapp_v1.ui.theme.IoTBlue
import com.example.ecobinapp_v1.ui.theme.MonoFamily
import com.example.ecobinapp_v1.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    Scaffold(topBar = { EcoTopBar("Credits", onBack = onBack) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoGreen, fontWeight = FontWeight.Bold)) { append("ECO") }
                    withStyle(SpanStyle(color = IoTBlue, fontWeight = FontWeight.Bold)) { append("BIN") }
                },
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Smart waste monitoring",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )

            Spacer(Modifier.height(28.dp))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(
                        "Development Team",
                        style = MaterialTheme.typography.titleSmall,
                        color = Ink
                    )
                    Spacer(Modifier.height(12.dp))
                    NameRow("Sasha Fratini")
                    NameRow("Lorenzo Fiore")
                    NameRow("Luigi Gaetano Flaccomio")
                }
            }

            Spacer(Modifier.height(12.dp))

            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                    MetaItem("Communication Protocol", "BLE 5.1 (Bluetooth Low Energy)")
                    Spacer(Modifier.height(14.dp))
                    MetaItem("Version", "1.0.0", mono = true)
                    Spacer(Modifier.height(14.dp))
                    MetaItem("Release Date", "June 2026")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NameRow(name: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(EcoGreen)
        )
        Spacer(Modifier.width(12.dp))
        Text(name, style = MaterialTheme.typography.bodyLarge, color = Ink)
    }
}

@Composable
private fun MetaItem(label: String, value: String, mono: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontFamily = if (mono) MonoFamily else null,
            color = Ink
        )
    }
}
