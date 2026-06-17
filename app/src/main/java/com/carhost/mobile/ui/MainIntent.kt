package com.carhost.mobile.ui

import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.CustomCommandDef
import com.carhost.mobile.data.model.QuickButtonDef
import com.carhost.mobile.data.model.VehicleCommand

sealed interface MainIntent {
    data class SelectTab(val tab: AppTab) : MainIntent
    data class UpdateHostDraft(val value: String) : MainIntent
    data class UpdatePortDraft(val value: String) : MainIntent
    data object SaveEndpoint : MainIntent
    data object ToggleConnection : MainIntent
    data class SendCommand(val command: VehicleCommand) : MainIntent
    data object ClearHistory : MainIntent
    data class SetDynamicColor(val enabled: Boolean) : MainIntent
    data class SetKeepScreenOn(val enabled: Boolean) : MainIntent
    data class SetNotifications(val enabled: Boolean) : MainIntent
    data object ClearMonitorData : MainIntent
    data class AddCustomChart(val chart: CustomChartDef) : MainIntent
    data class DeleteCustomChart(val chartId: String) : MainIntent
    data class UpdateCustomChart(val chart: CustomChartDef) : MainIntent
    data class SendRawCommand(val raw: String) : MainIntent
    data class UpdateCustomCommand(val command: CustomCommandDef) : MainIntent
    data class AddQuickButton(val button: QuickButtonDef) : MainIntent
    data class DeleteQuickButton(val buttonId: String) : MainIntent
    data class UpdateQuickButton(val button: QuickButtonDef) : MainIntent
    data object RestoreDefaultCommands : MainIntent
    data class ToggleBuiltInChartVisible(val chartId: String) : MainIntent
    data class UpdateBuiltInChart(val chartId: String, val name: String, val fieldPath: String) : MainIntent
    data class DeleteBuiltInChart(val chartId: String) : MainIntent
}
