package com.carhost.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carhost.mobile.data.local.PreferencesRepository
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val vehicleRepository: VehicleRepository,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(AppTab.Overview)
    private val hostDraft = MutableStateFlow(ConnectionProfile().host)
    private val portDraft = MutableStateFlow(ConnectionProfile().port.toString())
    private val endpointDraft = combine(hostDraft, portDraft) { host, port ->
        host to port
    }

    val uiState = combine(
        selectedTab,
        preferencesRepository.preferences,
        endpointDraft,
        vehicleRepository.telemetry,
        vehicleRepository.logs,
    ) { tab, preferences, endpointDraft, telemetry, logs ->
        MainUiState(
            selectedTab = tab,
            preferences = preferences,
            endpointDraftHost = endpointDraft.first,
            endpointDraftPort = endpointDraft.second,
            telemetry = telemetry,
            logs = logs,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    init {
        viewModelScope.launch {
            val preferences = preferencesRepository.preferences.first()
            hostDraft.value = preferences.endpoint.host
            portDraft.value = preferences.endpoint.port.toString()
        }
    }

    fun dispatch(intent: MainIntent) {
        when (intent) {
            is MainIntent.SelectTab -> selectedTab.value = intent.tab
            is MainIntent.UpdateHostDraft -> hostDraft.value = intent.value
            is MainIntent.UpdatePortDraft -> portDraft.value = intent.value.filter(Char::isDigit).take(5)
            MainIntent.SaveEndpoint -> saveEndpoint()
            MainIntent.ToggleConnection -> toggleConnection()
            is MainIntent.SendCommand -> sendCommand(intent.command)
            MainIntent.ClearHistory -> clearHistory()
            is MainIntent.SetDynamicColor -> setDynamicColor(intent.enabled)
            is MainIntent.SetKeepScreenOn -> setKeepScreenOn(intent.enabled)
            is MainIntent.SetNotifications -> setNotifications(intent.enabled)
        }
    }

    private fun saveEndpoint() {
        val host = hostDraft.value.trim().ifEmpty { ConnectionProfile().host }
        val port = portDraft.value.toIntOrNull()?.coerceIn(1, 65535) ?: ConnectionProfile().port

        hostDraft.update { host }
        portDraft.update { port.toString() }

        viewModelScope.launch {
            preferencesRepository.saveEndpoint(host, port)
        }
    }

    private fun toggleConnection() {
        viewModelScope.launch {
            val telemetry = vehicleRepository.telemetry.value
            if (telemetry.linkState == LinkState.Online || telemetry.linkState == LinkState.Connecting) {
                vehicleRepository.disconnect()
            } else {
                val profile = ConnectionProfile(
                    host = hostDraft.value.trim().ifEmpty { ConnectionProfile().host },
                    port = portDraft.value.toIntOrNull()?.coerceIn(1, 65535) ?: ConnectionProfile().port,
                )
                preferencesRepository.saveEndpoint(profile.host, profile.port)
                vehicleRepository.connect(profile)
            }
        }
    }

    private fun sendCommand(command: com.carhost.mobile.data.model.VehicleCommand) {
        viewModelScope.launch {
            vehicleRepository.sendCommand(command)
        }
    }

    private fun clearHistory() {
        viewModelScope.launch {
            vehicleRepository.clearHistory()
        }
    }

    private fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
        }
    }

    private fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setKeepScreenOn(enabled)
        }
    }

    private fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
        }
    }
}
