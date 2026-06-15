package com.carhost.mobile.data.repository

import com.carhost.mobile.data.local.db.LogRecordDao
import com.carhost.mobile.data.local.db.LogRecordEntity
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.LogEntry
import com.carhost.mobile.data.model.ReturnReason
import com.carhost.mobile.data.model.TelemetrySnapshot
import com.carhost.mobile.data.model.VehicleCommand
import com.carhost.mobile.data.model.VehicleState
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class TcpVehicleRepository @Inject constructor(
    private val logRecordDao: LogRecordDao,
) : VehicleRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val telemetryState = MutableStateFlow(TelemetrySnapshot())

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var readerJob: Job? = null

    override val telemetry: StateFlow<TelemetrySnapshot> = telemetryState.asStateFlow()

    override val logs: Flow<List<LogEntry>> = logRecordDao.observeRecent().map { rows ->
        rows.map { it.toModel() }
    }

    override suspend fun connect(profile: ConnectionProfile) {
        disconnect()
        appendLog("INFO", "正在连接 ${profile.host}:${profile.port}")
        telemetryState.update { it.copy(linkState = LinkState.Connecting, latencyMs = 0) }

        try {
            val connectedSocket = withContext(Dispatchers.IO) {
                Socket().apply {
                    connect(InetSocketAddress(profile.host, profile.port), CONNECT_TIMEOUT_MS)
                    soTimeout = READ_TIMEOUT_MS
                    tcpNoDelay = true
                }
            }

            socket = connectedSocket
            writer = BufferedWriter(OutputStreamWriter(connectedSocket.getOutputStream(), Charsets.UTF_8))
            telemetryState.update {
                it.copy(
                    linkState = LinkState.Connecting,
                    vehicleState = VehicleState.Idle,
                    latencyMs = 0,
                )
            }
            appendLog("ACK", "TCP 通道已建立，等待 telemetry 数据 ${profile.host}:${profile.port}")
            startReader(connectedSocket)
            sendWireCommand("status")
        } catch (throwable: Throwable) {
            closeSocket()
            telemetryState.update { it.copy(linkState = LinkState.Fault, latencyMs = 0) }
            appendLog("ERR", "连接失败：${throwable.message ?: throwable::class.java.simpleName}")
        }
    }

    override suspend fun disconnect() {
        readerJob?.cancel()
        readerJob = null
        closeSocket()
        telemetryState.update {
            it.copy(
                linkState = LinkState.Offline,
                vehicleState = VehicleState.Idle,
                speedMetersPerSecond = 0f,
                latencyMs = 0,
            )
        }
    }

    override suspend fun sendCommand(command: VehicleCommand) {
        val wire = command.toSmartCarCommand()
        appendLog("CMD", "发送命令 ${command.label} -> $wire")
        sendWireCommand(wire)
    }

    override suspend fun clearHistory() {
        logRecordDao.clear()
    }

    private fun startReader(connectedSocket: Socket) {
        readerJob?.cancel()
        readerJob = scope.launch {
            try {
                val reader = BufferedReader(InputStreamReader(connectedSocket.getInputStream(), Charsets.UTF_8))
                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.isNotBlank()) {
                        handleLine(line.trim())
                    }
                }
                appendLog("WARN", "TCP 连接已断开")
            } catch (throwable: Throwable) {
                appendLog("ERR", "接收失败：${throwable.message ?: throwable::class.java.simpleName}")
            } finally {
                closeSocket()
                telemetryState.update { it.copy(linkState = LinkState.Offline, latencyMs = 0) }
            }
        }
    }

    private suspend fun handleLine(line: String) {
        appendLog("RX", line)

        val json = runCatching { JSONObject(line) }.getOrNull()
        if (json == null) {
            appendLog("WARN", "忽略非 JSON 数据：$line")
            return
        }

        when (json.optString("type")) {
            "telemetry" -> updateTelemetry(json)
            "ack" -> appendLog("ACK", "${json.optString("cmd")} -> ${json.optString("result")}")
            "alert" -> appendLog("ALERT", line)
            "rfid" -> updateRfid(json)
            else -> appendLog("WARN", "未知 JSON 类型：${json.optString("type")}")
        }
    }

    private fun updateTelemetry(json: JSONObject) {
        val h2 = json.optDouble("h2", 0.0).toFloat()
        val temp = when {
            json.has("cab") -> json.optDouble("cab").toFloat()
            json.has("temp") -> json.optDouble("temp").toFloat()
            else -> telemetryState.value.temperatureC
        }
        val dist = json.optDouble("dist", telemetryState.value.obstacleDistanceCm.toDouble()).toInt()
        val battery = json.optInt("bat", telemetryState.value.batteryPercent).coerceIn(0, 100)
        val rfid = json.optInt("rfid", 0)
        val gas = normalizeGas(h2)
        val state = when {
            temp > 80f || gas >= 0.80f -> VehicleState.Evacuating
            temp >= 70f || gas >= 0.65f -> VehicleState.Alarm
            temp >= 60f || gas >= 0.45f -> VehicleState.Warning
            telemetryState.value.vehicleState == VehicleState.Idle -> VehicleState.Idle
            else -> VehicleState.Patrol
        }

        telemetryState.update { current ->
            current.copy(
                linkState = LinkState.Online,
                vehicleState = state,
                positionLabel = if (rfid > 0) "RFID-$rfid" else current.positionLabel,
                rfidTag = if (rfid > 0) "RFID-$rfid" else current.rfidTag,
                locationDescription = if (rfid > 0) "RFID 标签 $rfid" else current.locationDescription,
                temperatureC = temp,
                gasPercent = gas,
                batteryPercent = battery,
                obstacleDistanceCm = dist,
                obstacleDetected = dist in 1 until 10,
                returnReason = if (battery <= 20) ReturnReason.LowBattery else current.returnReason,
                temperatureHistory = current.temperatureHistory.roll(temp),
                gasHistory = current.gasHistory.roll(gas),
                batteryHistory = current.batteryHistory.roll(battery.toFloat()),
            )
        }
    }

    private fun updateRfid(json: JSONObject) {
        val id = json.optInt("id", 0)
        val location = json.optString("location", "unknown")
        telemetryState.update { current ->
            current.copy(
                rfidTag = "RFID-$id",
                positionLabel = location,
                locationDescription = location,
            )
        }
    }

    private suspend fun sendWireCommand(command: String) {
        val currentWriter = writer
        if (currentWriter == null || socket?.isConnected != true) {
            appendLog("ERR", "命令未发送：TCP 未连接")
            return
        }

        withContext(Dispatchers.IO) {
            currentWriter.write(command)
            currentWriter.write("\n")
            currentWriter.flush()
        }
    }

    private fun closeSocket() {
        runCatching { writer?.close() }
        runCatching { socket?.close() }
        writer = null
        socket = null
    }

    private suspend fun appendLog(level: String, message: String) {
        logRecordDao.insert(
            LogRecordEntity(
                timestamp = LocalTime.now().format(TimeFormatter),
                level = level,
                message = message,
            )
        )
        logRecordDao.trim()
    }

    private fun VehicleCommand.toSmartCarCommand(): String = when (this) {
        VehicleCommand.StartPatrol -> "start"
        VehicleCommand.Pause,
        VehicleCommand.EmergencyStop -> "stop"
        VehicleCommand.ManualReset -> "status"
        VehicleCommand.Evacuate -> "stop"
        VehicleCommand.ReturnHome -> "status"
    }

    private fun normalizeGas(h2: Float): Float = when {
        h2 <= 1f -> h2.coerceIn(0f, 1f)
        h2 <= 100f -> (h2 / 100f).coerceIn(0f, 1f)
        else -> (h2 / 4095f).coerceIn(0f, 1f)
    }

    private fun List<Float>.roll(next: Float, maxSamples: Int = 24): List<Float> {
        val merged = this + next
        return if (merged.size <= maxSamples) merged else merged.takeLast(maxSamples)
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 5000
        const val READ_TIMEOUT_MS = 0
        val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
