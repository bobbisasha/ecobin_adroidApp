package com.example.ecobinapp_v1.ble

import java.util.UUID

/** BLE UUIDs: service 0xFFF0, distance char 0xFFF4 (Notify), CCCD 0x2902. */
object BleConstants {

    /** 16-bit short code -> 128-bit UUID. */
    private fun uuid16(short: String): UUID =
        UUID.fromString("0000$short-0000-1000-8000-00805f9b34fb")

    /** Service. */
    val SERVICE_UUID: UUID = uuid16("fff0")

    /** Distance characteristic (Notify). */
    val DISTANCE_CHAR_UUID: UUID = uuid16("fff4")

    /** Notification descriptor. */
    val CCCD_UUID: UUID = uuid16("2902")
}
