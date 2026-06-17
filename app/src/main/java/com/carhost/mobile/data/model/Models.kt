package com.carhost.mobile.data.model

enum class AppTab(val label: String) {
    Overview("总览"),
    Monitor("监控"),
    Control("控制"),
    History("历史"),
    Settings("设置"),
}

enum class LinkState(val label: String) {
    Offline("离线"),
    Connecting("连接中"),
    Connected("已连接"),
    Online("在线"),
    Fault("故障"),
}

enum class VehicleState(val label: String) {
    Idle("待命"),
    Patrol("巡检中"),
    Warning("预警"),
    Alarm("报警"),
    Evacuating("紧急撤离"),
    Returning("返航中"),
    Avoiding("避障停车"),
    Manual("手动模式"),
    Paused("已暂停"),
    Emergency("急停"),
}

enum class AlertLevel(val label: String) {
    Normal("正常"),
    Warning("预警"),
    Alarm("告警"),
    Critical("紧急"),
}

enum class VehicleCommand(
    val label: String,
    val wireValue: String,
    val emphasis: CommandEmphasis,
) {
    StartPatrol("开始巡检", "start_patrol", CommandEmphasis.Primary),
    Idle("待机任务", "idle", CommandEmphasis.Neutral),
    Evacuate("紧急撤离", "evacuate", CommandEmphasis.Accent),
    TemperatureWarning("温度预警", "temp_warning", CommandEmphasis.Danger),
    TemperatureAlarm("温度警告", "temp_alarm", CommandEmphasis.Accent),
    ReturnHome("返航回桩", "return_home", CommandEmphasis.Neutral),
}

enum class CommandEmphasis {
    Primary,
    Accent,
    Danger,
    Neutral,
}

enum class ReturnReason(val label: String) {
    None("无"),
    Manual("手动返航"),
    LowBattery("低电返航"),
    Emergency("紧急撤离"),
}

data class ConnectionProfile(
    val host: String = "192.168.4.1",
    val port: Int = 8080,
)

data class CustomCommandDef(
    val commandId: String = "",
    val customLabel: String = "",
    val customWireValue: String = "",
)

data class QuickButtonDef(
    val id: String = java.util.UUID.randomUUID().toString(),
    val label: String = "",
    val wireValue: String = "",
)

data class OperatorPreferences(
    val endpoint: ConnectionProfile = ConnectionProfile(),
    val useDynamicColor: Boolean = true,
    val keepScreenOn: Boolean = true,
    val notificationsEnabled: Boolean = false,
    val customCharts: List<CustomChartDef> = emptyList(),
    val customCommands: List<CustomCommandDef> = emptyList(),
    val quickButtons: List<QuickButtonDef> = emptyList(),
)

data class ChartPoint(
    val timeLabel: String,
    val value: Float,
)

data class TrackPoint(
    val timeLabel: String,
    val binary: String,
)

enum class CustomChartType { Line, Bar }

data class CustomChartDef(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val fieldPath: String = "",
    val unit: String = "",
    val type: CustomChartType = CustomChartType.Line,
)

data class TelemetrySnapshot(
    val linkState: LinkState = LinkState.Offline,
    val vehicleState: VehicleState = VehicleState.Idle,
    val positionLabel: String = "START",
    val rfidTag: String = "RFID-START",
    val locationDescription: String = "起点 / 充电桩",
    val temperatureC: Float = 0f,
    val gasPercent: Float = 0f,
    val mq8Raw: Float = 0f,
    val ahtTemperatureC: Float = 0f,
    val ahtHumidityPercent: Float = 0f,
    val mlxObjectTemperatureC: Float = 0f,
    val mlxAmbientTemperatureC: Float = 0f,
    val trackValue: Int = 0,
    val trackBinary: String = "0000",
    val batteryPercent: Int = 0,
    val speedMetersPerSecond: Float = 0f,
    val latencyMs: Int = 0,
    val obstacleDistanceCm: Int = 0,
    val obstacleDetected: Boolean = false,
    val returnReason: ReturnReason = ReturnReason.None,
    val homeDockReached: Boolean = false,
    val temperatureHistory: List<ChartPoint> = emptyList(),
    val gasHistory: List<ChartPoint> = emptyList(),
    val batteryHistory: List<ChartPoint> = emptyList(),
    val speedHistory: List<ChartPoint> = emptyList(),
    val mlxObjectHistory: List<ChartPoint> = emptyList(),
    val ahtTemperatureHistory: List<ChartPoint> = emptyList(),
    val ahtHumidityHistory: List<ChartPoint> = emptyList(),
    val mq8History: List<ChartPoint> = emptyList(),
    val trackHistory: List<TrackPoint> = emptyList(),
    val rfidHistory: List<String> = emptyList(),
    val lastReceivedJson: String = "",
    val lastSentJson: String = "",
    val customChartHistory: Map<String, List<ChartPoint>> = emptyMap(),
) {
    val alertLevel: AlertLevel
        get() = when {
            mlxObjectTemperatureC > 80f || gasPercent >= 0.80f -> AlertLevel.Critical
            mlxObjectTemperatureC >= 70f || gasPercent >= 0.65f -> AlertLevel.Alarm
            mlxObjectTemperatureC >= 60f || gasPercent >= 0.45f -> AlertLevel.Warning
            else -> AlertLevel.Normal
        }
}

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val level: String,
    val message: String,
)
