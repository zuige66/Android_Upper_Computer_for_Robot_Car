package com.carhost.mobile.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.carhost.mobile.data.local.PreferencesRepository
import com.carhost.mobile.data.local.db.LogRecordDao
import com.carhost.mobile.data.local.db.LogRecordEntity
import com.carhost.mobile.data.model.ChartPoint
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.LogEntry
import com.carhost.mobile.data.model.ReturnReason
import com.carhost.mobile.data.model.TelemetrySnapshot
import com.carhost.mobile.data.model.TrackPoint
import com.carhost.mobile.data.model.VehicleCommand
import com.carhost.mobile.data.model.VehicleState
import com.carhost.mobile.data.notify.AlertNotifier
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Singleton
class TcpVehicleRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logRecordDao: LogRecordDao,
    private val preferencesRepository: PreferencesRepository,
    private val alertNotifier: AlertNotifier,
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
            val wifiNetwork = findWifiNetwork()
            if (wifiNetwork == null) {
                appendLog("WARN", "未发现 WiFi 网络，手机可能会走蜂窝网络导致 ESP 连接超时")
            } else {
                appendLog("INFO", "已选择 WiFi 网络连接小车")
            }

            val connectedSocket = withContext(Dispatchers.IO) {
                (wifiNetwork?.socketFactory?.createSocket() ?: Socket()).apply {
                    connect(InetSocketAddress(profile.host, profile.port), CONNECT_TIMEOUT_MS)
                    soTimeout = READ_TIMEOUT_MS
                    tcpNoDelay = true
                }
            }

            socket = connectedSocket
            writer = BufferedWriter(OutputStreamWriter(connectedSocket.getOutputStream(), Charsets.UTF_8))
            telemetryState.update {
                it.copy(
                    linkState = LinkState.Connected,
                    vehicleState = VehicleState.Idle,
                    latencyMs = 0,
                )
            }
            appendLog(
                "ACK",
                "TCP 通道已建立，本机 ${connectedSocket.localAddress.hostAddress}，已连接 ${profile.host}:${profile.port}",
            )
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
        telemetryState.update { current -> current.applyCommandState(command) }
    }

    override suspend fun clearHistory() {
        logRecordDao.clear()
    }

    override suspend fun clearMonitorHistory() {
        telemetryState.update { current ->
            current.copy(
                temperatureHistory = emptyList(),
                gasHistory = emptyList(),
                batteryHistory = emptyList(),
                speedHistory = emptyList(),
                mlxObjectHistory = emptyList(),
                ahtTemperatureHistory = emptyList(),
                ahtHumidityHistory = emptyList(),
                mq8History = emptyList(),
                trackHistory = emptyList(),
                rfidHistory = emptyList(),
                customChartHistory = emptyMap(),
            )
        }
    }

    override suspend fun resetTelemetry() {
        val currentLink = telemetryState.value.linkState
        telemetryState.value = TelemetrySnapshot(linkState = currentLink)
    }

    override suspend fun sendRawCommand(raw: String) {
        appendLog("CMD", "自定义发送 -> $raw")
        sendWireCommand(raw)
        telemetryState.update { it.copy(lastSentJson = raw) }
    }

    private fun findWifiNetwork(): Network? {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java) ?: return null
        val activeNetwork = connectivityManager.activeNetwork

        if (activeNetwork != null &&
            connectivityManager.getNetworkCapabilities(activeNetwork)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        ) {
            return activeNetwork
        }

        return connectivityManager.allNetworks.firstOrNull { network ->
            connectivityManager.getNetworkCapabilities(network)
                ?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }
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
        telemetryState.update { it.copy(lastReceivedJson = line) }

        val json = runCatching { JSONObject(line) }.getOrNull()
        if (json == null) {
            appendLog("WARN", "忽略非 JSON 数据：$line")
            return
        }

        when (json.optString("type")) {
            "telemetry" -> updateTelemetry(json)
            "ack" -> {
                telemetryState.update { it.copy(linkState = LinkState.Online, latencyMs = 0) }
                appendLog("ACK", "${json.optString("cmd")} -> ${json.optString("result")}")
            }
            "alert" -> {
                appendLog("ALERT", line)
                updateTelemetry(json)
                maybeSendAlertNotification(json)
            }
            "rfid" -> updateRfid(json)
            else -> appendLog("WARN", "未知 JSON 类型：${json.optString("type")}")
        }
    }

    private suspend fun maybeSendAlertNotification(json: JSONObject) {
        val notificationsEnabled = preferencesRepository.preferences.first().notificationsEnabled
        if (!notificationsEnabled) return

        val state = json.optString("state").ifBlank { "unknown" }
        val mlxObj = json.optDouble("MLX_obj", telemetryState.value.mlxObjectTemperatureC.toDouble()).toFloat()
        val mq8 = json.optDouble("MQ8", telemetryState.value.mq8Raw.toDouble()).toFloat()
        val content = "状态 $state，红外目标温度 ${"%.1f".format(mlxObj)}°C，MQ8 ${mq8.toInt()}"
        alertNotifier.showAlert("巡检小车报警", content)
    }

    private fun updateTelemetry(json: JSONObject) {
        val current = telemetryState.value
        val mq8 = when {
            json.has("MQ8") -> json.optDouble("MQ8").toFloat()
            json.has("h2") -> json.optDouble("h2").toFloat()
            else -> current.mq8Raw
        }
        val ahtTemp = when {
            json.has("AHT_temp") -> json.optDouble("AHT_temp").toFloat()
            json.has("ATH_temp") -> json.optDouble("ATH_temp").toFloat()
            json.has("temp") -> json.optDouble("temp").toFloat()
            else -> current.ahtTemperatureC
        }
        val ahtHum = when {
            json.has("AHT_hum") -> json.optDouble("AHT_hum").toFloat()
            json.has("hum") -> json.optDouble("hum").toFloat()
            else -> current.ahtHumidityPercent
        }
        val mlxObj = when {
            json.has("MLX_obj") -> json.optDouble("MLX_obj").toFloat()
            json.has("cab") -> json.optDouble("cab").toFloat()
            else -> current.mlxObjectTemperatureC
        }
        val mlxAmb = when {
            json.has("MLX_amb") -> json.optDouble("MLX_amb").toFloat()
            else -> current.mlxAmbientTemperatureC
        }
        val dist = json.optDouble("dist", current.obstacleDistanceCm.toDouble()).toInt()
        val battery = json.optInt("bat", current.batteryPercent).coerceIn(0, 100)
        val rfid = json.optInt("rfid", 0)
        val rfidLoc = json.optString("rfid_loc", "").takeIf { it.isNotBlank() && it.lowercase() != "unknown" }
        val trackValue = json.optInt("track", current.trackValue).coerceIn(0, 15)
        val trackBinary = json.optString("track_bin").takeIf { it.matches(Regex("[01]{4}")) }
            ?: trackValue.toString(2).padStart(4, '0').takeLast(4)
        val gas = normalizeGas(mq8)
        val timeLabel = LocalTime.now().format(ChartTimeFormatter)
        val state = when {
            mlxObj > 80f || gas >= 0.80f -> VehicleState.Evacuating
            mlxObj >= 70f || gas >= 0.65f -> VehicleState.Alarm
            mlxObj >= 60f || gas >= 0.45f -> VehicleState.Warning
            dist in 1 until 10 -> VehicleState.Avoiding
            current.vehicleState in setOf(
                VehicleState.Patrol,
                VehicleState.Returning,
                VehicleState.Paused,
                VehicleState.Manual,
                VehicleState.Evacuating,
                VehicleState.Emergency,
            ) -> current.vehicleState
            else -> VehicleState.Idle
        }

        val newRfidLabel = rfidLoc ?: if (rfid > 0) "RFID-$rfid" else null
        val newRfidHistory = if (newRfidLabel != null && newRfidLabel != current.rfidHistory.lastOrNull()) {
            (current.rfidHistory + newRfidLabel).let { if (it.size > 24) it.takeLast(24) else it }
        } else {
            current.rfidHistory
        }

        telemetryState.update { current ->
            current.copy(
                linkState = LinkState.Online,
                vehicleState = state,
                positionLabel = newRfidLabel ?: current.positionLabel,
                rfidTag = newRfidLabel ?: current.rfidTag,
                locationDescription = newRfidLabel ?: current.locationDescription,
                temperatureC = mlxObj,
                gasPercent = gas,
                mq8Raw = mq8,
                ahtTemperatureC = ahtTemp,
                ahtHumidityPercent = ahtHum,
                mlxObjectTemperatureC = mlxObj,
                mlxAmbientTemperatureC = mlxAmb,
                trackValue = trackValue,
                trackBinary = trackBinary,
                batteryPercent = battery,
                obstacleDistanceCm = dist,
                obstacleDetected = dist in 1 until 10,
                returnReason = if (battery <= 20) ReturnReason.LowBattery else current.returnReason,
                temperatureHistory = current.temperatureHistory.roll(ChartPoint(timeLabel, mlxObj)),
                gasHistory = current.gasHistory.roll(ChartPoint(timeLabel, gas)),
                batteryHistory = current.batteryHistory.roll(ChartPoint(timeLabel, battery.toFloat())),
                mlxObjectHistory = current.mlxObjectHistory.roll(ChartPoint(timeLabel, mlxObj)),
                ahtTemperatureHistory = current.ahtTemperatureHistory.roll(ChartPoint(timeLabel, ahtTemp)),
                ahtHumidityHistory = current.ahtHumidityHistory.roll(ChartPoint(timeLabel, ahtHum)),
                mq8History = current.mq8History.roll(ChartPoint(timeLabel, mq8)),
                trackHistory = current.trackHistory.roll(TrackPoint(timeLabel, trackBinary)),
                rfidHistory = newRfidHistory,
            )
        }
    }

    private fun updateRfid(json: JSONObject) {
        val id = when {
            json.has("rfid") -> json.optInt("rfid", 0)
            json.has("rfid_id") -> json.optInt("rfid_id", 0)
            else -> json.optInt("id", 0)
        }
        val location = json.optString("location", "unknown")
        telemetryState.update { current ->
            val label = location.takeIf { it != "unknown" } ?: "RFID-$id"
            val newHistory = if (label != current.rfidHistory.lastOrNull()) {
                (current.rfidHistory + label).let { if (it.size > 24) it.takeLast(24) else it }
            } else {
                current.rfidHistory
            }
            current.copy(
                rfidTag = "RFID-$id",
                positionLabel = location,
                locationDescription = location,
                rfidHistory = newHistory,
            )
        }
    }

    private suspend fun sendWireCommand(command: String) {
        val currentWriter = writer
        if (currentWriter == null || socket?.isConnected != true) {
            appendLog("ERR", "命令未发送：TCP 未连接")
            return
        }

        telemetryState.update { it.copy(lastSentJson = command) }
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
        VehicleCommand.Idle -> "idle"
        VehicleCommand.TemperatureWarning -> "temp_warning"
        VehicleCommand.TemperatureAlarm -> "temp_alarm"
        VehicleCommand.Evacuate -> "evacuate"
        VehicleCommand.ReturnHome -> "return_home"
    }

    private fun TelemetrySnapshot.applyCommandState(command: VehicleCommand): TelemetrySnapshot = when (command) {
        VehicleCommand.StartPatrol -> copy(
            vehicleState = VehicleState.Patrol,
            speedMetersPerSecond = 0.35f,
            homeDockReached = false,
            returnReason = ReturnReason.None,
        )
        VehicleCommand.Idle -> copy(
            vehicleState = VehicleState.Idle,
            speedMetersPerSecond = 0f,
            returnReason = ReturnReason.None,
        )
        VehicleCommand.Evacuate -> copy(
            vehicleState = VehicleState.Evacuating,
            speedMetersPerSecond = 0.45f,
            returnReason = ReturnReason.Emergency,
            homeDockReached = false,
        )
        VehicleCommand.TemperatureWarning -> copy(
            vehicleState = VehicleState.Warning,
            speedMetersPerSecond = 0f,
        )
        VehicleCommand.TemperatureAlarm -> copy(
            vehicleState = VehicleState.Alarm,
            speedMetersPerSecond = 0f,
        )
        VehicleCommand.ReturnHome -> copy(
            vehicleState = VehicleState.Returning,
            speedMetersPerSecond = 0.28f,
            returnReason = ReturnReason.Manual,
            homeDockReached = false,
        )
    }

    private fun normalizeGas(h2: Float): Float = when {
        h2 <= 1f -> h2.coerceIn(0f, 1f)
        h2 <= 100f -> (h2 / 100f).coerceIn(0f, 1f)
        else -> (h2 / 4095f).coerceIn(0f, 1f)
    }

    private fun <T> List<T>.roll(next: T, maxSamples: Int = 24): List<T> {
        val merged = this + next
        return if (merged.size <= maxSamples) merged else merged.takeLast(maxSamples)
    }

    private companion object {
        const val CONNECT_TIMEOUT_MS = 5000
        const val READ_TIMEOUT_MS = 0
        val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val ChartTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }
}
