package com.carhost.mobile.data.repository

import com.carhost.mobile.data.local.db.LogRecordDao
import com.carhost.mobile.data.local.db.LogRecordEntity
import com.carhost.mobile.data.model.AlertLevel
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class FakeVehicleRepository @Inject constructor(
    private val logRecordDao: LogRecordDao,
) : VehicleRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val routeNodes = listOf(
        RouteCheckpoint("START", "RFID-START", "起点 / 充电桩"),
        RouteCheckpoint("A1", "RFID-A1", "A 区 1 号巡检点"),
        RouteCheckpoint("A2", "RFID-A2", "A 区 2 号工位"),
        RouteCheckpoint("B2", "RFID-B2", "B 区 2 号弯道"),
        RouteCheckpoint("B3", "RFID-B3", "B 区 3 号通道"),
    )
    private val telemetryState = MutableStateFlow(TelemetrySnapshot())

    private var simulationJob: Job? = null
    private var routeIndex = 0
    private var lastAlertLevel = AlertLevel.Normal

    override val telemetry: StateFlow<TelemetrySnapshot> = telemetryState.asStateFlow()

    override val logs: Flow<List<LogEntry>> = logRecordDao.observeRecent().map { rows ->
        rows.map { it.toModel() }
    }

    override suspend fun connect(profile: ConnectionProfile) {
        if (telemetryState.value.linkState == LinkState.Online) {
            return
        }

        appendLog("INFO", "正在连接 ${profile.host}:${profile.port}")
        telemetryState.update { it.copy(linkState = LinkState.Connecting, latencyMs = 0) }
        delay(700)
        telemetryState.update { current ->
            current.copy(
                linkState = LinkState.Online,
                vehicleState = VehicleState.Idle,
                latencyMs = 96,
            )
        }
        appendLog("ACK", "TCP 会话已建立")
        startSimulation()
    }

    override suspend fun disconnect() {
        simulationJob?.cancel()
        simulationJob = null
        telemetryState.update {
            it.copy(
                linkState = LinkState.Offline,
                vehicleState = VehicleState.Idle,
                speedMetersPerSecond = 0f,
                latencyMs = 0,
                obstacleDetected = false,
                obstacleDistanceCm = 120,
            )
        }
        appendLog("INFO", "操作员已断开连接")
    }

    override suspend fun sendCommand(command: VehicleCommand) {
        appendLog("CMD", "发送命令 ${command.wireValue}")

        telemetryState.update { current ->
            when (command) {
                VehicleCommand.StartPatrol -> current.copy(
                    vehicleState = VehicleState.Patrol,
                    speedMetersPerSecond = 0.42f,
                    returnReason = ReturnReason.None,
                    homeDockReached = false,
                )

                VehicleCommand.Idle -> current.copy(
                    vehicleState = VehicleState.Idle,
                    speedMetersPerSecond = 0f,
                    returnReason = ReturnReason.None,
                )

                VehicleCommand.Evacuate -> current.copy(
                    vehicleState = VehicleState.Evacuating,
                    speedMetersPerSecond = 0.58f,
                    returnReason = ReturnReason.Emergency,
                    homeDockReached = false,
                )

                VehicleCommand.TemperatureWarning -> current.copy(
                    vehicleState = VehicleState.Warning,
                    speedMetersPerSecond = 0f,
                )

                VehicleCommand.TemperatureAlarm -> current.copy(
                    vehicleState = VehicleState.Alarm,
                    speedMetersPerSecond = 0f,
                )

                VehicleCommand.ReturnHome -> current.copy(
                    vehicleState = VehicleState.Returning,
                    speedMetersPerSecond = 0.36f,
                    returnReason = ReturnReason.Manual,
                    homeDockReached = false,
                )
            }
        }

        appendLog("ACK", "${command.label} 已确认")
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
        telemetryState.update { it.copy(lastSentJson = raw) }
    }

    private fun startSimulation() {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            while (true) {
                delay(1300)
                val previous = telemetryState.value
                val nextCheckpoint = routeNodes[(routeIndex + 1) % routeNodes.size]
                val next = previous.advance(nextCheckpoint)
                routeIndex = (routeIndex + 1) % routeNodes.size
                telemetryState.value = next
                maybeEmitRouteLogs(previous, next)
                maybeEmitSafetyLogs(previous, next)
                maybeEmitAlertLogs(previous.alertLevel, next.alertLevel, next)
            }
        }
    }

    private suspend fun maybeEmitRouteLogs(
        previous: TelemetrySnapshot,
        current: TelemetrySnapshot,
    ) {
        if (previous.rfidTag != current.rfidTag) {
            appendLog("RFID", "识别到 ${current.rfidTag}，位置 ${current.locationDescription}")
        }
    }

    private suspend fun maybeEmitSafetyLogs(
        previous: TelemetrySnapshot,
        current: TelemetrySnapshot,
    ) {
        if (!previous.obstacleDetected && current.obstacleDetected) {
            appendLog("OBST", "前方障碍 ${current.obstacleDistanceCm} cm，已触发避障停车")
        } else if (previous.obstacleDetected && !current.obstacleDetected) {
            appendLog("INFO", "障碍物解除，继续执行状态机")
        }

        if (previous.returnReason != current.returnReason) {
            when (current.returnReason) {
                ReturnReason.LowBattery -> appendLog("RETURN", "电量低于阈值，自动返航")
                ReturnReason.Manual -> appendLog("RETURN", "操作员触发手动返航")
                ReturnReason.Emergency -> appendLog("RETURN", "高危事件触发撤离/返航逻辑")
                ReturnReason.None -> Unit
            }
        }

        if (!previous.homeDockReached && current.homeDockReached) {
            appendLog("ACK", "已抵达起点 / 充电桩")
        }
    }

    private suspend fun maybeEmitAlertLogs(
        previousLevel: AlertLevel,
        currentLevel: AlertLevel,
        telemetry: TelemetrySnapshot,
    ) {
        if (currentLevel == previousLevel && currentLevel == lastAlertLevel) {
            return
        }
        lastAlertLevel = currentLevel

        when (currentLevel) {
            AlertLevel.Normal -> appendLog("INFO", "巡检环境已恢复稳定")
            AlertLevel.Warning -> appendLog("WARN", "红外目标温度进入预警区：${telemetry.mlxObjectTemperatureC.toInt()}°C")
            AlertLevel.Alarm -> appendLog("ALARM", "高温/气体异常，建议准备撤离")
            AlertLevel.Critical -> appendLog("CRITICAL", "达到紧急阈值，已切入撤离态")
        }
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

    private fun TelemetrySnapshot.advance(nextCheckpoint: RouteCheckpoint): TelemetrySnapshot {
        val cruising = vehicleState in setOf(
            VehicleState.Patrol,
            VehicleState.Evacuating,
            VehicleState.Returning,
        )
        val nextBattery = if (cruising) max(8, batteryPercent - 1) else min(100, batteryPercent + 1)
        val nextLatency = when (linkState) {
            LinkState.Online -> (74..138).random()
            LinkState.Connecting -> 220
            else -> 0
        }

        val nextTemp = when (vehicleState) {
            VehicleState.Patrol -> min(84f, temperatureC + 3.6f)
            VehicleState.Warning -> min(82f, temperatureC + 2.4f)
            VehicleState.Alarm -> min(86f, temperatureC + 1.6f)
            VehicleState.Evacuating, VehicleState.Returning -> max(36f, temperatureC - 2.8f)
            VehicleState.Avoiding,
            VehicleState.Emergency,
            VehicleState.Paused,
            VehicleState.Manual -> max(32f, temperatureC - 1.4f)
            VehicleState.Idle -> max(28f, temperatureC - 0.8f)
        }

        val nextGas = when (vehicleState) {
            VehicleState.Patrol -> min(0.84f, gasPercent + 0.05f)
            VehicleState.Warning -> min(0.78f, gasPercent + 0.04f)
            VehicleState.Alarm -> min(0.88f, gasPercent + 0.03f)
            VehicleState.Evacuating, VehicleState.Returning -> max(0.12f, gasPercent - 0.05f)
            VehicleState.Avoiding -> max(0.10f, gasPercent - 0.03f)
            else -> max(0.08f, gasPercent - 0.02f)
        }
        val nextMq8Raw = nextGas * 4095f
        val nextAhtTemp = max(24f, nextTemp - 3.2f)
        val nextAhtHum = min(85f, max(35f, 56f + nextGas * 18f))
        val nextMlxAmb = max(24f, nextAhtTemp - 0.8f)
        val nextTrackValue = if (cruising) (nextBattery % 16) else trackValue
        val nextTrackBinary = nextTrackValue.toString(2).padStart(4, '0').takeLast(4)
        val timeLabel = LocalTime.now().format(ChartTimeFormatter)

        val baseReturnReason = when {
            nextTemp > 80f || nextGas >= 0.80f -> ReturnReason.Emergency
            nextBattery <= 20 && vehicleState != VehicleState.Emergency -> ReturnReason.LowBattery
            vehicleState == VehicleState.Returning && returnReason != ReturnReason.None -> returnReason
            vehicleState == VehicleState.Evacuating -> ReturnReason.Emergency
            else -> ReturnReason.None
        }

        val preObstacleState = when {
            linkState != LinkState.Online -> VehicleState.Idle
            nextTemp > 80f || nextGas >= 0.80f -> VehicleState.Evacuating
            nextTemp >= 70f || nextGas >= 0.65f -> VehicleState.Alarm
            nextTemp >= 60f || nextGas >= 0.45f -> VehicleState.Warning
            nextBattery <= 20 && vehicleState != VehicleState.Emergency -> VehicleState.Returning
            vehicleState == VehicleState.Paused ||
                vehicleState == VehicleState.Manual ||
                vehicleState == VehicleState.Emergency -> vehicleState
            else -> VehicleState.Patrol
        }

        val obstacleDistance = when {
            linkState != LinkState.Online -> 999
            preObstacleState in setOf(VehicleState.Patrol, VehicleState.Evacuating) &&
                nextCheckpoint.label == "B2" &&
                nextBattery % 7 == 0 -> 8
            cruising -> 24 + (nextBattery % 70)
            else -> 120
        }

        val obstacleDetected = obstacleDistance < 10 &&
            preObstacleState in setOf(VehicleState.Patrol, VehicleState.Evacuating)

        val autoState = if (obstacleDetected) {
            VehicleState.Avoiding
        } else {
            preObstacleState
        }

        val nextSpeed = when (autoState) {
            VehicleState.Patrol -> 0.42f
            VehicleState.Warning -> 0.34f
            VehicleState.Alarm -> 0.28f
            VehicleState.Evacuating -> 0.58f
            VehicleState.Returning -> 0.36f
            VehicleState.Avoiding -> 0f
            else -> 0f
        }

        val nextPosition = if (cruising) nextCheckpoint.label else positionLabel
        val nextRfidTag = if (cruising) nextCheckpoint.rfidTag else rfidTag
        val nextLocation = if (cruising) nextCheckpoint.description else locationDescription
        val nextHomeDockReached = autoState == VehicleState.Returning && nextCheckpoint.label == "START"

        val nextRfidHistory = if (cruising && nextPosition != rfidHistory.lastOrNull()) {
            (rfidHistory + nextPosition).let { if (it.size > 24) it.takeLast(24) else it }
        } else {
            rfidHistory
        }

        val simJson = """{"type":"telemetry","MQ8":${nextMq8Raw.toInt()},"AHT_temp":$nextAhtTemp,"AHT_hum":$nextAhtHum,"MLX_obj":$nextTemp,"MLX_amb":$nextMlxAmb,"dist":$obstacleDistance,"bat":$nextBattery,"track":$nextTrackValue,"track_bin":"$nextTrackBinary","rfid_loc":"$nextPosition"}"""

        return copy(
            vehicleState = autoState,
            positionLabel = nextPosition,
            rfidTag = nextRfidTag,
            locationDescription = nextLocation,
            temperatureC = nextTemp,
            gasPercent = nextGas,
            mq8Raw = nextMq8Raw,
            ahtTemperatureC = nextAhtTemp,
            ahtHumidityPercent = nextAhtHum,
            mlxObjectTemperatureC = nextTemp,
            mlxAmbientTemperatureC = nextMlxAmb,
            trackValue = nextTrackValue,
            trackBinary = nextTrackBinary,
            batteryPercent = nextBattery,
            speedMetersPerSecond = nextSpeed,
            latencyMs = nextLatency,
            obstacleDistanceCm = obstacleDistance,
            obstacleDetected = obstacleDetected,
            returnReason = baseReturnReason,
            homeDockReached = nextHomeDockReached,
            temperatureHistory = temperatureHistory.roll(ChartPoint(timeLabel, nextTemp)),
            gasHistory = gasHistory.roll(ChartPoint(timeLabel, nextGas)),
            batteryHistory = batteryHistory.roll(ChartPoint(timeLabel, nextBattery.toFloat())),
            speedHistory = speedHistory.roll(ChartPoint(timeLabel, nextSpeed)),
            mlxObjectHistory = mlxObjectHistory.roll(ChartPoint(timeLabel, nextTemp)),
            ahtTemperatureHistory = ahtTemperatureHistory.roll(ChartPoint(timeLabel, nextAhtTemp)),
            ahtHumidityHistory = ahtHumidityHistory.roll(ChartPoint(timeLabel, nextAhtHum)),
            mq8History = mq8History.roll(ChartPoint(timeLabel, nextMq8Raw)),
            trackHistory = trackHistory.roll(TrackPoint(timeLabel, nextTrackBinary)),
            rfidHistory = nextRfidHistory,
            lastReceivedJson = simJson,
        )
    }

    private fun <T> List<T>.roll(next: T, maxSamples: Int = 24): List<T> {
        val merged = this + next
        return if (merged.size <= maxSamples) merged else merged.takeLast(maxSamples)
    }

    private companion object {
        val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val ChartTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    private data class RouteCheckpoint(
        val label: String,
        val rfidTag: String,
        val description: String,
    )
}
