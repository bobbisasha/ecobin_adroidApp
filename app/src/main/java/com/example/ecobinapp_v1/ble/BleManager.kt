package com.example.ecobinapp_v1.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.sin

/** A scanned or demo device. */
data class DiscoveredDevice(
    val name: String?,
    val address: String,
    val rssi: Int
) {
    val displayName: String get() = name?.takeIf { it.isNotBlank() } ?: "Unnamed device"
}

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED, FAILED }

/** Flow-based BLE scan/connect/notify. Demo mode simulates a bin (emulator has no BLE). */
@SuppressLint("MissingPermission") // permissions checked by callers
class BleManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val bluetoothManager =
        appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter

    // State
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _discovered = MutableStateFlow<List<DiscoveredDevice>>(emptyList())
    val discovered: StateFlow<List<DiscoveredDevice>> = _discovered.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    /** Latest distance (cm). */
    private val _distanceCm = MutableStateFlow<Int?>(null)
    val distanceCm: StateFlow<Int?> = _distanceCm.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    /** Demo simulation flag. */
    private val _demoMode = MutableStateFlow(isProbablyEmulator() || adapter == null)
    val demoMode: StateFlow<Boolean> = _demoMode.asStateFlow()

    // Internals
    private var gatt: BluetoothGatt? = null
    private var scanJob: Job? = null
    private var demoJob: Job? = null
    private var pollJob: Job? = null

    fun isBluetoothSupported(): Boolean = adapter != null
    fun isBluetoothEnabled(): Boolean = adapter?.isEnabled == true

    fun setDemoMode(enabled: Boolean) {
        if (_demoMode.value == enabled) return
        stopScan()
        disconnect()
        _demoMode.value = enabled
    }

    /** Required runtime permissions. */
    fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

    // Scanning
    fun startScan() {
        _discovered.value = emptyList()

        if (_demoMode.value) {
            _isScanning.value = true
            _status.value = "Scanning (demo)…"
            scanJob?.cancel()
            scanJob = scope.launch {
                delay(800)
                _discovered.value = listOf(
                    DiscoveredDevice("EcoBin Simulator", DEMO_ADDRESS, -42),
                    DiscoveredDevice("EcoBin Simulator #2", "DE:M0:00:00:00:02", -67)
                )
                _isScanning.value = false
                _status.value = "Demo device found"
            }
            return
        }

        val scanner = adapter?.bluetoothLeScanner
        if (scanner == null || !isBluetoothEnabled()) {
            _status.value = "Bluetooth is off"
            return
        }

        _isScanning.value = true
        _status.value = "Scanning for nearby bins…"

        // No filter: list all nearby devices.
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, scanCallback)

        scanJob?.cancel()
        scanJob = scope.launch {
            delay(SCAN_PERIOD_MS)
            stopScan()
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        if (!_demoMode.value && isBluetoothEnabled()) {
            runCatching { adapter?.bluetoothLeScanner?.stopScan(scanCallback) }
        }
        if (_isScanning.value) {
            _isScanning.value = false
            _status.value = if (_discovered.value.isEmpty()) "No devices found" else "Scan finished"
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = DiscoveredDevice(
                name = result.scanRecord?.deviceName,
                address = result.device.address,
                rssi = result.rssi
            )
            val current = _discovered.value.filter { it.address != device.address }
            _discovered.value = (current + device).sortedByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            _isScanning.value = false
            _status.value = "Scan failed (code $errorCode)"
        }
    }

    // Connecting
    fun connect(address: String) {
        disconnect()
        stopScan()
        _distanceCm.value = null
        _connectionState.value = ConnectionState.CONNECTING

        if (_demoMode.value) {
            _status.value = "Connecting (demo)…"
            demoJob = scope.launch {
                delay(700)
                _connectionState.value = ConnectionState.CONNECTED
                _status.value = "Connected (demo)"
                var t = 0.0
                while (isActive) {
                    // Simulated reading.
                    val distance = (41 + 39 * sin(t)).roundToInt().coerceIn(0, 100)
                    _distanceCm.value = distance
                    t += 0.35
                    delay(1500)
                }
            }
            return
        }

        val dev = runCatching { adapter?.getRemoteDevice(address) }.getOrNull()
        if (dev == null) {
            _connectionState.value = ConnectionState.FAILED
            _status.value = "Invalid device address"
            return
        }
        _status.value = "Connecting…"
        gatt = dev.connectGatt(appContext, false, gattCallback, BluetoothDevice_TRANSPORT_LE)
    }

    fun disconnect() {
        demoJob?.cancel(); demoJob = null
        pollJob?.cancel(); pollJob = null
        gatt?.let {
            runCatching { it.disconnect() }
            runCatching { it.close() }
        }
        gatt = null
        if (_connectionState.value != ConnectionState.DISCONNECTED) {
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    _status.value = "Discovering services…"
                    g.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runCatching { g.close() }
                    gatt = null
                    pollJob?.cancel(); pollJob = null
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _status.value = "Disconnected"
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            val char = g.getService(BleConstants.SERVICE_UUID)
                ?.getCharacteristic(BleConstants.DISTANCE_CHAR_UUID)
            if (char == null) {
                _connectionState.value = ConnectionState.FAILED
                _status.value = "Distance characteristic not found"
                return
            }
            _connectionState.value = ConnectionState.CONNECTED
            _status.value = "Connected"

            val canNotify =
                char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
            val canRead =
                char.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0

            if (canNotify) enableNotifications(g, char) // subscribe
            if (canRead) g.readCharacteristic(char)     // initial read
            if (!canNotify) startPolling(g, char)       // fallback
        }

        // Legacy (< API 33)
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.DISTANCE_CHAR_UUID) handleValue(characteristic.value)
        }

        override fun onCharacteristicChanged(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            if (characteristic.uuid == BleConstants.DISTANCE_CHAR_UUID) handleValue(value)
        }

        // Legacy (< API 33)
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS &&
                characteristic.uuid == BleConstants.DISTANCE_CHAR_UUID
            ) {
                handleValue(characteristic.value)
            }
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS &&
                characteristic.uuid == BleConstants.DISTANCE_CHAR_UUID
            ) {
                handleValue(value)
            }
        }
    }

    private fun enableNotifications(g: BluetoothGatt, char: BluetoothGattCharacteristic) {
        g.setCharacteristicNotification(char, true)
        val cccd = char.getDescriptor(BleConstants.CCCD_UUID) ?: return
        if (Build.VERSION.SDK_INT >= 33) {
            g.writeDescriptor(cccd, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } else {
            @Suppress("DEPRECATION")
            run {
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                g.writeDescriptor(cccd)
            }
        }
    }

    private fun startPolling(g: BluetoothGatt, char: BluetoothGattCharacteristic) {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive) {
                delay(POLL_INTERVAL_MS)
                runCatching { g.readCharacteristic(char) }
            }
        }
    }

    private fun handleValue(bytes: ByteArray?) {
        val distance = parseDistanceCm(bytes) ?: return
        _distanceCm.value = distance
    }

    companion object Parser {
        /** Parse "Dist: <N> cm" payload to cm; null if malformed. */
        fun parseDistanceCm(payload: ByteArray?): Int? {
            if (payload == null || payload.isEmpty()) return null

            // Up to null terminator.
            val nul = payload.indexOf(0.toByte())
            val length = if (nul >= 0) nul else payload.size
            if (length == 0) return null
            val text = String(payload, 0, length, Charsets.US_ASCII).trim()

            val matched = DIST_REGEX.find(text)?.groupValues?.getOrNull(1)
            val number = matched ?: INT_REGEX.find(text)?.value // fallback: any integer
            return number?.toIntOrNull()
        }

        private val DIST_REGEX = Regex("""Dist:\s*(\d+)\s*cm""", RegexOption.IGNORE_CASE)
        private val INT_REGEX = Regex("""\d+""")

        const val DEMO_ADDRESS = "DE:M0:00:00:00:01"
        private const val SCAN_PERIOD_MS = 12_000L
        private const val POLL_INTERVAL_MS = 2_000L

        // = BluetoothDevice.TRANSPORT_LE
        private const val BluetoothDevice_TRANSPORT_LE = 2
    }

    fun close() {
        disconnect()
        stopScan()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun isProbablyEmulator(): Boolean {
        val fp = Build.FINGERPRINT.lowercase()
        val model = Build.MODEL.lowercase()
        val product = Build.PRODUCT.lowercase()
        val hardware = Build.HARDWARE.lowercase()
        return fp.startsWith("generic") ||
            fp.startsWith("unknown") ||
            fp.contains("emulator") ||
            model.contains("emulator") ||
            model.contains("android sdk built for") ||
            product.contains("sdk") ||
            product.contains("emulator") ||
            hardware.contains("goldfish") ||
            hardware.contains("ranchu")
    }
}
