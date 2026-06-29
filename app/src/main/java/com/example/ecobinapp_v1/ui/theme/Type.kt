package com.example.ecobinapp_v1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Typography system
 * -----------------
 * Design calls for Inter (headings + body) and JetBrains Mono (data / sensor values).
 *
 * To keep the app fully offline and dependency-free, we use the platform sans-serif for text and
 * the platform monospace for data, while honoring the weight hierarchy (Bold/700 headings,
 * Regular/400 body). To drop in the real typefaces later:
 *   1. Add the .ttf files to app/src/main/res/font/ (e.g. inter_regular.ttf, inter_bold.ttf,
 *      jetbrains_mono_regular.ttf), then
 *   2. set  AppSans = FontFamily(Font(R.font.inter_regular), Font(R.font.inter_bold, FontWeight.Bold))
 *      and  MonoFamily = FontFamily(Font(R.font.jetbrains_mono_regular))
 */
val AppSans: FontFamily = FontFamily.Default

/** Used for sensor values, IDs, hex codes and other raw data strings. */
val MonoFamily: FontFamily = FontFamily.Monospace

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Bold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)
