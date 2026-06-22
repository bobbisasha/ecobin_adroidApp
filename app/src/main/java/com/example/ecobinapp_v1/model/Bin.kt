package com.example.ecobinapp_v1.model

/**
 * A garbage bin the user has registered.
 *
 * The device reports CHAR5 = the distance (in cm) measured by a sensor mounted at the top of the
 * bin to the surface of the trash below. When the bin is empty the distance is large (≈ depth);
 * when full the distance is small (≈ 0). Fullness is therefore derived from [depthCm].
 *
 * @param id                 stable unique id (UUID string)
 * @param name               user-friendly label, e.g. "Kitchen bin"
 * @param depthCm            internal depth of the empty bin in cm (set at registration); maps a
 *                           distance reading to a fullness percentage
 * @param deviceAddress      BLE MAC address used to reconnect to this bin's device
 * @param deviceName         advertised device name (for display only, may be null)
 * @param lastDistanceCm     last distance read from CHAR5, or null if never connected
 * @param lastUpdatedEpochMs when the last reading was taken (millis since epoch), 0 if never
 */
data class Bin(
    val id: String,
    val name: String,
    val depthCm: Int,
    val deviceAddress: String,
    val deviceName: String? = null,
    val lastDistanceCm: Int? = null,
    val lastUpdatedEpochMs: Long = 0L
) {
    /**
     * Fullness percentage in 0..100, derived from the last known distance and the bin depth.
     * Returns null when there is no reading yet.
     */
    val fullnessPercent: Int?
        get() = lastDistanceCm?.let { fullnessPercent(it, depthCm) }

    companion object {
        /**
         * Convert a raw distance (cm) into a fullness percentage given the bin [depthCm].
         *
         * full  -> distance ~ 0    -> 100%
         * empty -> distance ~ depth -> 0%
         */
        fun fullnessPercent(distanceCm: Int, depthCm: Int): Int {
            if (depthCm <= 0) return 0
            val filledCm = (depthCm - distanceCm).toFloat()
            val pct = (filledCm / depthCm.toFloat()) * 100f
            return pct.coerceIn(0f, 100f).toInt()
        }
    }
}
