package com.example.ecobinapp_v1.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ecobinapp_v1.R
import com.example.ecobinapp_v1.ui.theme.EcoGreen
import com.example.ecobinapp_v1.ui.theme.Hairline
import com.example.ecobinapp_v1.ui.theme.IoTBlue
import com.example.ecobinapp_v1.ui.theme.IoTBlueSoft
import com.example.ecobinapp_v1.ui.theme.Muted
import com.example.ecobinapp_v1.ui.theme.SurfaceCanvas
import com.example.ecobinapp_v1.ui.theme.SurfaceWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onContinue: () -> Unit) {
    val scale = remember { Animatable(0.82f) }
    val fade = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch { fade.animateTo(1f, tween(500)) }
        launch {
            scale.animateTo(
                1f,
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
        }
        delay(1500)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to SurfaceWhite,
                    0.55f to SurfaceCanvas,
                    1f to IoTBlueSoft.copy(alpha = 0.35f)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(SurfaceWhite)
                    .border(BorderStroke(1.dp, Hairline), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_ecobin_mark),
                    contentDescription = null,
                    modifier = Modifier.size(116.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = EcoGreen, fontWeight = FontWeight.Bold)) { append("ECO") }
                    withStyle(SpanStyle(color = IoTBlue, fontWeight = FontWeight.Bold)) { append("BIN") }
                },
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.alpha(fade.value)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Smart waste monitoring",
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
                modifier = Modifier.alpha(fade.value)
            )
        }
    }
}
