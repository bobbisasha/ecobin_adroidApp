package com.example.ecobinapp_v1.data

import android.content.Context
import com.example.ecobinapp_v1.model.Bin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Tiny persistence layer for registered bins, backed by SharedPreferences + JSON.
 *
 * Deliberately dependency-free (no Room/DataStore) to keep the app as simple as possible.
 * The current list is exposed as a [StateFlow] so the UI recomposes on every change.
 */
class BinRepository(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _bins = MutableStateFlow(load())
    val bins: StateFlow<List<Bin>> = _bins.asStateFlow()

    /** Add a new bin or replace an existing one with the same id. */
    fun upsert(bin: Bin) {
        val updated = _bins.value.filter { it.id != bin.id } + bin
        persist(updated.sortedBy { it.name.lowercase() })
    }

    fun delete(binId: String) {
        persist(_bins.value.filter { it.id != binId })
    }

    fun getById(binId: String): Bin? = _bins.value.firstOrNull { it.id == binId }

    /** Store a fresh reading for a bin (called while connected). */
    fun updateReading(binId: String, distanceCm: Int, timestampMs: Long) {
        val updated = _bins.value.map {
            if (it.id == binId) {
                it.copy(lastDistanceCm = distanceCm, lastUpdatedEpochMs = timestampMs)
            } else {
                it
            }
        }
        persist(updated)
    }

    private fun persist(list: List<Bin>) {
        _bins.value = list
        val arr = JSONArray()
        list.forEach { bin ->
            arr.put(
                JSONObject().apply {
                    put("id", bin.id)
                    put("name", bin.name)
                    put("depthCm", bin.depthCm)
                    put("deviceAddress", bin.deviceAddress)
                    put("deviceName", bin.deviceName ?: JSONObject.NULL)
                    if (bin.lastDistanceCm != null) put("lastDistanceCm", bin.lastDistanceCm)
                    put("lastUpdatedEpochMs", bin.lastUpdatedEpochMs)
                }
            )
        }
        prefs.edit().putString(KEY_BINS, arr.toString()).apply()
    }

    private fun load(): List<Bin> {
        val raw = prefs.getString(KEY_BINS, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Bin(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    depthCm = o.getInt("depthCm"),
                    deviceAddress = o.getString("deviceAddress"),
                    deviceName = if (o.isNull("deviceName")) null else o.optString("deviceName"),
                    lastDistanceCm = if (o.has("lastDistanceCm")) o.getInt("lastDistanceCm") else null,
                    lastUpdatedEpochMs = o.optLong("lastUpdatedEpochMs", 0L)
                )
            }.sortedBy { it.name.lowercase() }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val PREFS_NAME = "ecobin_prefs"
        private const val KEY_BINS = "bins"
    }
}
