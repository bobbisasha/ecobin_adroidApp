package com.example.ecobinapp_v1.ui.theme

import androidx.compose.ui.graphics.Color

/* ---- Brand ------------------------------------------------------------- */
val EcoGreen = Color(0xFF10B981)      // Primary — "Bin Safe", recycling, brand accent
val EcoGreenDark = Color(0xFF059669)
val EcoGreenSoft = Color(0xFFD1FAE5)  // tint backgrounds
val IoTBlue = Color(0xFF3B82F6)       // Secondary — BLE status, connectivity
val IoTBlueDark = Color(0xFF2563EB)
val IoTBlueSoft = Color(0xFFDBEAFE)

/* ---- Neutrals (slate) -------------------------------------------------- */
val Ink = Color(0xFF0F172A)           // headings (slate-900)
val InkBody = Color(0xFF334155)       // strong body (slate-700)
val Muted = Color(0xFF64748B)         // secondary text (slate-500)
val Hairline = Color(0xFFE5E7EB)      // card borders (gray-200)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceCanvas = Color(0xFFF8FAFC) // app background (slate-50)
val ChipBg = Color(0xFFF1F5F9)        // mono chip / subtle fill (slate-100)

/* ---- Status (fullness) ------------------------------------------------- */
val StatusSafe = Color(0xFF10B981)     // < 80%   green
val StatusWarn = Color(0xFFF59E0B)     // 80–94%  orange
val StatusCritical = Color(0xFFEF4444) // >= 95%  red
val GaugeTrack = Color(0xFFE5E7EB)
val MutedGrey = Color(0xFF94A3B8)      // no-reading state

/* ---- Dark theme variants ---------------------------------------------- */
val InkDark = Color(0xFF0B1220)
val SurfaceDark = Color(0xFF111827)
val HairlineDark = Color(0xFF1F2937)
val OnDark = Color(0xFFE5E7EB)
val MutedDark = Color(0xFF94A3B8)
