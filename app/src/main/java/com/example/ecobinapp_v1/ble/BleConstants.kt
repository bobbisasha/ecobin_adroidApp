package com.example.ecobinapp_v1.ble

import java.util.UUID

/**
 * BLE UUIDs for the bin firmware.
 *
 * Defaults below match the Texas Instruments "SimpleProfile" layout (CC254x / CC264x family),
 * where the service is 0xFFF0 and CHAR1..CHAR5 are 0xFFF1..0xFFF5. CHAR5 carries the distance
 * reading (cm).
 *
 * If your firmware uses different (e.g. 128-bit custom) UUIDs, change ONLY the three values below.
 */
object BleConstants {

    /** Helper to build a 128-bit UUID from a 16-bit Bluetooth SIG short code. */
    private fun uuid16(short: String): UUID =
        UUID.fromString("0000$short-0000-1000-8000-00805f9b34fb")

    /** SimpleProfile service that contains CHAR5. */
    val SERVICE_UUID: UUID = uuid16("fff0")

    /** CHAR5 — the distance-to-trash reading, in centimeters. */
    val CHAR5_UUID: UUID = uuid16("fff5")

    /** Standard Client Characteristic Configuration Descriptor (for enabling notifications). */
    val CCCD_UUID: UUID = uuid16("2902")
}
