package com.example.ecobinapp_v1.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ecobinapp_v1.ui.theme.FillHigh
import com.example.ecobinapp_v1.ui.theme.FillLow
import com.example.ecobinapp_v1.ui.theme.FillMedium
import com.example.ecobinapp_v1.ui.theme.GaugeTrack

/** Color coding for fullness: green < 50, amber 50-79, red >= 80. */
fun fillColor(percent: Int?): Color = when {
    percent == null -> Color(0xFFBDBDBD)
    percent >= 80 -> FillHigh
    percent >= 50 -> FillMedium
    else -> FillLow
}

/**
 * A circular fullness gauge. Shows the percentage in the center and a colored arc proportional to
 * fullness. Pass null for [percent] to render an empty "no reading" state.
 */
@Composable
fun FullnessGauge(
    percent: Int?,
    modifier: Modifier = Modifier,
    diameter: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
    labelSize: Int = 34
) {
    val arcColor = fillColor(percent)
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(inset, inset)

            // Track
            drawArc(
                color = GaugeTrack,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // Fill
            val sweep = ((percent ?: 0).coerceIn(0, 100)) / 100f * 360f
            if (sweep > 0f) {
                drawArc(
                    color = arcColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = percent?.let { "$it%" } ?: "—",
            fontSize = labelSize.sp,
            fontWeight = FontWeight.Bold,
            color = if (percent == null) MaterialTheme.colorScheme.onSurfaceVariant else arcColor
        )
    }
}
