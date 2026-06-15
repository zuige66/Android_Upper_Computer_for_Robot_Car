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
    Pause("暂停任务", "pause", CommandEmphasis.Neutral),
    Evacuate("紧急撤离", "evacuate", CommandEmphasis.Accent),
    EmergencyStop("立即急停", "emergency_stop", CommandEmphasis.Danger),
    ManualReset("手动复位", "manual_reset", CommandEmphasis.Neutral),
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

data class OperatorPreferences(
    val endpoint: ConnectionProfile = ConnectionProfile(),
    val useDynamicColor: Boolean = true,
    val keepScreenOn: Boolean = true,
    val notificationsEnabled: Boolean = false,
)

data class TelemetrySnapshot(
    val linkState: LinkState = LinkState.Offline,
    val vehicleState: VehicleState = VehicleState.Idle,
    val positionLabel: String = "START",
    val rfidTag: String = "RFID-START",
    val locationDescription: String = "起点 / 充电桩",
    val temperatureC: Float = 0f,
    val gasPercent: Float = 0f,
    val batteryPercent: Int = 0,
    val speedMetersPerSecond: Float = 0f,
    val latencyMs: Int = 0,
    val obstacleDistanceCm: Int = 0,
    val obstacleDetected: Boolean = false,
    val returnReason: ReturnReason = ReturnReason.None,
    val homeDockReached: Boolean = false,
    val temperatureHistory: List<Float> = emptyList(),
    val gasHistory: List<Float> = emptyList(),
    val batteryHistory: List<Float> = emptyList(),
    val speedHistory: List<Float> = emptyList(),
) {
    val alertLevel: AlertLevel
        get() = when {
            temperatureC > 80f || gasPercent >= 0.80f -> AlertLevel.Critical
            temperatureC >= 70f || gasPercent >= 0.65f -> AlertLevel.Alarm
            temperatureC >= 60f || gasPercent >= 0.45f -> AlertLevel.Warning
            else -> AlertLevel.Normal
        }
}

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val level: String,
    val message: String,
)
