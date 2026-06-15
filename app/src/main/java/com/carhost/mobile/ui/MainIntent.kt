package com.carhost.mobile.ui

import com.carhost.mobile.data.model.AppTab
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
}
