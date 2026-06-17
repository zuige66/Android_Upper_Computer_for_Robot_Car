package com.carhost.mobile.ui

import com.carhost.mobile.data.model.AlertLevel
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.CustomCommandDef
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.QuickButtonDef
import com.carhost.mobile.data.model.LogEntry
import com.carhost.mobile.data.model.OperatorPreferences
import com.carhost.mobile.data.model.TelemetrySnapshot
import com.carhost.mobile.data.model.VehicleCommand

data class BuiltInChartDef(
    val id: String,
    val defaultName: String,
    val subtitle: String,
    val defaultFieldPath: String,
)

data class BuiltInChartState(
    val visible: Boolean = true,
    val customName: String? = null,
    val customFieldPath: String? = null,
)

data class MainUiState(
    val selectedTab: AppTab = AppTab.Control,
    val preferences: OperatorPreferences = OperatorPreferences(),
    val endpointDraftHost: String = ConnectionProfile().host,
    val endpointDraftPort: String = ConnectionProfile().port.toString(),
    val telemetry: TelemetrySnapshot = TelemetrySnapshot(),
    val logs: List<LogEntry> = emptyList(),
    val availableCommands: List<VehicleCommand> = VehicleCommand.entries,
    val customCommands: List<CustomCommandDef> = emptyList(),
    val quickButtons: List<QuickButtonDef> = emptyList(),
    val customCharts: List<CustomChartDef> = emptyList(),
    val builtInChartStates: Map<String, BuiltInChartState> = emptyMap(),
    val builtInCharts: List<BuiltInChartDef> = emptyList(),
) {
    val connected: Boolean
        get() = telemetry.linkState == LinkState.Online || telemetry.linkState == LinkState.Connected

    val connectionActive: Boolean
        get() = telemetry.linkState == LinkState.Online || telemetry.linkState == LinkState.Connected || telemetry.linkState == LinkState.Connecting

    val activeAlert: AlertLevel
        get() = telemetry.alertLevel
}
