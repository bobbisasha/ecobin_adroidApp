# EcoBin

A minimal Android app that reads the fullness of garbage bins over Bluetooth Low Energy (BLE).

Each bin device runs the same firmware and exposes a **TI SimpleProfile** service. The app reads
**CHAR5**, which carries the **distance in centimeters** measured by a sensor at the top of the bin
to the surface of the trash. Fullness is derived from the bin's depth:

```
fullness% = (depth_cm − distance_cm) / depth_cm × 100   (clamped to 0–100)
```

## How it works

1. **Add bin** – scan for the nearby device, pick it, give it a name and enter the bin **depth (cm)**.
2. The bin is saved (locally, on the device) and appears on the home screen with a circular gauge.
3. **Tap a bin** to connect; CHAR5 updates stream in live (via notifications, or polling if the
   characteristic doesn't support notify) and the gauge + percentage update in real time.
4. The last reading is remembered for each bin so the home screen shows the most recent fullness.

Multiple bins can be registered (each with its own depth); only one is connected at a time.

## Demo mode (for the emulator)

The standard Android emulator has **no Bluetooth radio**, so real scanning/connecting can't work
there. The app includes a **Demo mode** that simulates a bin streaming changing distance values.

- It **auto-enables on emulators** (and on devices with no BLE adapter).
- You can toggle it manually on the *Add a bin* screen.

This lets you exercise the entire UI on the emulator.

## Where to change BLE settings

All the firmware-specific bits are isolated so you can adapt them without touching the UI:

| What | File |
|------|------|
| Service / CHAR5 / CCCD UUIDs | `app/.../ble/BleConstants.kt` |
| How raw CHAR5 bytes → distance (cm) | `BleManager.parseDistance(...)` in `ble/BleManager.kt` |
| Fullness formula | `Bin.fullnessPercent(...)` in `model/Bin.kt` |

Defaults: service `0xFFF0`, CHAR5 `0xFFF5`, CCCD `0x2902`. `parseDistance` reads a 1-byte unsigned
value, or a little-endian `uint16` if 2+ bytes are sent.

## Permissions

- Android 12+ : `BLUETOOTH_SCAN` (with `neverForLocation`) and `BLUETOOTH_CONNECT`
- Android 11 and below : `BLUETOOTH`, `BLUETOOTH_ADMIN`, `ACCESS_FINE_LOCATION`

The app requests these at runtime the first time you scan/connect (skipped in demo mode).
