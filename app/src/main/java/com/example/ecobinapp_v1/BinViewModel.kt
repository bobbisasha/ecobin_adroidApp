package com.example.ecobinapp_v1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecobinapp_v1.ble.BleManager
import com.example.ecobinapp_v1.ble.DiscoveredDevice
import com.example.ecobinapp_v1.data.BinRepository
import com.example.ecobinapp_v1.model.Bin
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/** UI state holder: owns the repo + BLE manager, persists readings for the active bin. */
class BinViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BinRepository(app)
    val ble = BleManager(app)

    val bins: StateFlow<List<Bin>> = repo.bins

    private var activeBinId: String? = null

    init {
        // Persist each new reading.
        viewModelScope.launch {
            ble.distanceCm.collect { distance ->
                val id = activeBinId
                if (distance != null && id != null) {
                    repo.updateReading(id, distance, System.currentTimeMillis())
                }
            }
        }
    }

    fun binById(id: String): Bin? = repo.getById(id)

    fun registerBin(name: String, depthCm: Int, device: DiscoveredDevice): Bin {
        val bin = Bin(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifBlank { device.displayName },
            depthCm = depthCm,
            deviceAddress = device.address,
            deviceName = device.name
        )
        repo.upsert(bin)
        return bin
    }

    fun updateBinDetails(bin: Bin, newName: String, newDepthCm: Int) {
        repo.upsert(bin.copy(name = newName.trim().ifBlank { bin.name }, depthCm = newDepthCm))
    }

    fun deleteBin(id: String) {
        if (activeBinId == id) disconnect()
        repo.delete(id)
    }

    // BLE pass-throughs
    fun startScan() = ble.startScan()
    fun stopScan() = ble.stopScan()
    fun setDemoMode(enabled: Boolean) = ble.setDemoMode(enabled)

    fun connectToBin(bin: Bin) {
        activeBinId = bin.id
        ble.connect(bin.deviceAddress)
    }

    fun disconnect() {
        activeBinId = null
        ble.disconnect()
    }

    override fun onCleared() {
        ble.close()
    }
}
