package com.example.ecobinapp_v1.ble

import java.util.UUID

/**
 * BLE UUIDs for the bin firmware.
 *
 * Service 0xFFF0 contains the distance characteristic 0xFFF4 (Notify). The device pushes a
 * notification every ~5 s carrying a null-terminated ASCII string of the form "Dist: <N> cm".
 *
 * If your firmware uses different (e.g. 128-bit custom) UUIDs, change ONLY the values below.
 */
object BleConstants {

    /** Helper to build a 128-bit UUID from a 16-bit Bluetooth SIG short code. */
    private fun uuid16(short: String): UUID =
        UUID.fromString("0000$short-0000-1000-8000-00805f9b34fb")

    /** Service that contains the distance characteristic. */
    val SERVICE_UUID: UUID = uuid16("fff0")

    /** CHAR4 — Notify characteristic carrying the "Dist: <N> cm" string. */
    val DISTANCE_CHAR_UUID: UUID = uuid16("fff4")

    /** Standard Client Characteristic Configuration Descriptor (for enabling notifications). */
    val CCCD_UUID: UUID = uuid16("2902")
}
