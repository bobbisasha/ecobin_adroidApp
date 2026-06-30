package com.example.ecobinapp_v1.model

/** A registered bin. Fullness is derived from sensor distance and [depthCm]. */
data class Bin(
    val id: String,
    val name: String,
    val depthCm: Int,
    val deviceAddress: String,
    val deviceName: String? = null,
    val lastDistanceCm: Int? = null,
    val lastUpdatedEpochMs: Long = 0L
) {
    /** Fullness 0..100, or null if no reading. */
    val fullnessPercent: Int?
        get() = lastDistanceCm?.let { fullnessPercent(it, depthCm) }

    companion object {
        /** distance->fullness%: empty (≈depth)=0%, full (≈0)=100%. */
        fun fullnessPercent(distanceCm: Int, depthCm: Int): Int {
            if (depthCm <= 0) return 0
            val filledCm = (depthCm - distanceCm).toFloat()
            val pct = (filledCm / depthCm.toFloat()) * 100f
            return pct.coerceIn(0f, 100f).toInt()
        }
    }
}
