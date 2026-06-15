package com.carhost.mobile.ui

import com.carhost.mobile.data.model.AlertLevel
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.LogEntry
import com.carhost.mobile.data.model.OperatorPreferences
import com.carhost.mobile.data.model.TelemetrySnapshot
import com.carhost.mobile.data.model.VehicleCommand

data class MainUiState(
    val selectedTab: AppTab = AppTab.Overview,
    val preferences: OperatorPreferences = OperatorPreferences(),
    val endpointDraftHost: String = ConnectionProfile().host,
    val endpointDraftPort: String = ConnectionProfile().port.toString(),
    val telemetry: TelemetrySnapshot = TelemetrySnapshot(),
    val logs: List<LogEntry> = emptyList(),
    val availableCommands: List<VehicleCommand> = VehicleCommand.entries,
) {
    val connected: Boolean
        get() = telemetry.linkState == LinkState.Online

    val connectionActive: Boolean
        get() = telemetry.linkState == LinkState.Online || telemetry.linkState == LinkState.Connecting

    val activeAlert: AlertLevel
        get() = telemetry.alertLevel
}
