package com.carhost.mobile.data.repository

import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.LogEntry
import com.carhost.mobile.data.model.TelemetrySnapshot
import com.carhost.mobile.data.model.VehicleCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface VehicleRepository {
    val telemetry: StateFlow<TelemetrySnapshot>
    val logs: Flow<List<LogEntry>>

    suspend fun connect(profile: ConnectionProfile)
    suspend fun disconnect()
    suspend fun sendCommand(command: VehicleCommand)
    suspend fun sendRawCommand(raw: String)
    suspend fun clearHistory()
    suspend fun clearMonitorHistory()
}
