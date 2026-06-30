package com.example.ecobinapp_v1.ui

/** "time ago" label. */
fun formatAgo(epochMs: Long, nowMs: Long = System.currentTimeMillis()): String {
    if (epochMs <= 0L) return "never"
    val secs = ((nowMs - epochMs) / 1000).coerceAtLeast(0)
    return when {
        secs < 10 -> "just now"
        secs < 60 -> "${secs}s ago"
        secs < 3600 -> "${secs / 60}m ago"
        secs < 86_400 -> "${secs / 3600}h ago"
        else -> "${secs / 86_400}d ago"
    }
}
