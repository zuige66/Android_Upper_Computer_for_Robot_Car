package com.carhost.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carhost.mobile.data.local.PreferencesRepository
import com.carhost.mobile.data.model.AppTab
import com.carhost.mobile.data.model.ChartPoint
import com.carhost.mobile.data.model.ColorTheme
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.CustomCommandDef
import com.carhost.mobile.data.model.LinkState
import com.carhost.mobile.data.model.QuickButtonDef
import com.carhost.mobile.data.model.VehicleState
import com.carhost.mobile.data.notify.AlertNotifier
import com.carhost.mobile.data.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val vehicleRepository: VehicleRepository,
    private val alertNotifier: AlertNotifier,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(AppTab.Control)
    private val hostDraft = MutableStateFlow(ConnectionProfile().host)
    private val portDraft = MutableStateFlow(ConnectionProfile().port.toString())
    private val endpointDraft = combine(hostDraft, portDraft) { host, port ->
        host to port
    }
    private val builtInChartStates = MutableStateFlow<Map<String, BuiltInChartState>>(emptyMap())
    private val customChartHistory = MutableStateFlow<Map<String, List<ChartPoint>>>(emptyMap())
    private val overviewItemStates = MutableStateFlow<Map<String, OverviewItemState>>(emptyMap())
    private var autoReturnJob: Job? = null
    private var lastNotifiedState: VehicleState? = null

    private val coreState = combine(
        selectedTab,
        preferencesRepository.preferences,
        endpointDraft,
        vehicleRepository.telemetry,
        vehicleRepository.logs,
    ) { tab, preferences, endpointDraft, telemetry, logs ->
        arrayOf(tab, preferences, endpointDraft, telemetry, logs)
    }

    val uiState = combine(
        coreState,
        builtInChartStates,
        customChartHistory,
        overviewItemStates,
    ) { core, chartStates, cch, overviewStates ->
        val tab = core[0] as AppTab
        val preferences = core[1] as com.carhost.mobile.data.model.OperatorPreferences
        @Suppress("UNCHECKED_CAST")
        val endpointDraft = core[2] as Pair<String, String>
        val telemetry = core[3] as com.carhost.mobile.data.model.TelemetrySnapshot
        val logs = core[4] as List<com.carhost.mobile.data.model.LogEntry>

        val builtInCharts = DEFAULT_BUILT_IN_CHARTS.map { def ->
            val state = chartStates[def.id] ?: BuiltInChartState()
            BuiltInChartDef(
                id = def.id,
                defaultName = state.customName ?: def.defaultName,
                subtitle = def.subtitle,
                defaultFieldPath = state.customFieldPath ?: def.defaultFieldPath,
            )
        }

        MainUiState(
            selectedTab = tab,
            preferences = preferences,
            endpointDraftHost = endpointDraft.first,
            endpointDraftPort = endpointDraft.second,
            telemetry = telemetry.copy(customChartHistory = cch),
            logs = logs,
            customCommands = preferences.customCommands,
            quickButtons = preferences.quickButtons,
            customCharts = preferences.customCharts,
            builtInChartStates = chartStates,
            builtInCharts = builtInCharts,
            overviewItemStates = overviewStates,
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

        // Accumulate custom chart history in a separate coroutine
        viewModelScope.launch {
            combine(
                vehicleRepository.telemetry.map { it.lastReceivedJson }.distinctUntilChanged(),
                preferencesRepository.preferences.map { it.customCharts }.distinctUntilChanged(),
            ) { json, charts -> json to charts }
                .collect { (json, charts) ->
                    if (json.isNotBlank() && charts.isNotEmpty()) {
                        processCustomChartHistory(json, charts)
                    }
                }
        }

        // Notify on vehicle state changes
        viewModelScope.launch {
            vehicleRepository.telemetry.collect { telemetry ->
                val currentState = telemetry.vehicleState
                val prevState = lastNotifiedState
                if (prevState != null && prevState != currentState) {
                    val notificationsEnabled = preferencesRepository.preferences.first().notificationsEnabled
                    if (notificationsEnabled && alertNotifier.canNotify()) {
                        val title = "小车状态变化"
                        val content = "${prevState.label} → ${currentState.label}"
                        alertNotifier.showAlert(title, content)
                    }
                }
                lastNotifiedState = currentState
            }
        }

        // Auto-return to patrol after alarm states if temperature drops
        viewModelScope.launch {
            vehicleRepository.telemetry.collect { telemetry ->
                val isAlarmState = telemetry.vehicleState in setOf(
                    VehicleState.Warning, VehicleState.Alarm, VehicleState.Evacuating,
                )
                val tempNormal = telemetry.mlxObjectTemperatureC < 60f && telemetry.gasPercent < 0.45f

                if (isAlarmState && tempNormal && autoReturnJob == null) {
                    autoReturnJob = launch {
                        delay(5000)
                        val current = vehicleRepository.telemetry.value
                        if (current.vehicleState in setOf(VehicleState.Warning, VehicleState.Alarm, VehicleState.Evacuating)) {
                            vehicleRepository.sendCommand(com.carhost.mobile.data.model.VehicleCommand.StartPatrol)
                        }
                        autoReturnJob = null
                    }
                } else if (!isAlarmState || !tempNormal) {
                    autoReturnJob?.cancel()
                    autoReturnJob = null
                }
            }
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
            is MainIntent.SetColorTheme -> setColorTheme(intent.theme)
            is MainIntent.SetContrastLevel -> setContrastLevel(intent.level)
            MainIntent.ClearMonitorData -> clearMonitorData()
            is MainIntent.AddCustomChart -> addCustomChart(intent.chart)
            is MainIntent.DeleteCustomChart -> deleteCustomChart(intent.chartId)
            is MainIntent.UpdateCustomChart -> updateCustomChart(intent.chart)
            is MainIntent.SendRawCommand -> sendRawCommand(intent.raw)
            is MainIntent.UpdateCustomCommand -> updateCustomCommand(intent.command, intent.saveAsDefault)
            is MainIntent.AddQuickButton -> addQuickButton(intent.button, intent.saveAsDefault)
            is MainIntent.DeleteQuickButton -> deleteQuickButton(intent.buttonId, intent.saveAsDefault)
            is MainIntent.UpdateQuickButton -> updateQuickButton(intent.button, intent.saveAsDefault)
            MainIntent.RestoreDefaultCommands -> restoreDefaultCommands()
            is MainIntent.ToggleBuiltInChartVisible -> toggleBuiltInChartVisible(intent.chartId)
            is MainIntent.UpdateBuiltInChart -> updateBuiltInChart(intent.chartId, intent.name, intent.fieldPath, intent.saveAsDefault)
            is MainIntent.DeleteBuiltInChart -> deleteBuiltInChart(intent.chartId, intent.saveAsDefault)
            MainIntent.RestoreDefaultCharts -> restoreDefaultCharts()
            is MainIntent.DeleteOverviewItem -> deleteOverviewItem(intent.itemId, intent.saveAsDefault)
            is MainIntent.EditOverviewItem -> editOverviewItem(intent.itemId, intent.newTitle, intent.fieldPath, intent.saveAsDefault)
            MainIntent.RestoreDefaultOverview -> restoreDefaultOverview()
            MainIntent.ResetOverviewData -> resetOverviewData()
        }
    }

    private fun processCustomChartHistory(lastJson: String, customCharts: List<CustomChartDef>) {
        val json = runCatching { JSONObject(lastJson) }.getOrNull() ?: return
        val timeLabel = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))

        customChartHistory.update { current ->
            val updated = current.toMutableMap()
            customCharts.forEach { chart ->
                val value = parseJsonFieldPath(json, chart.fieldPath) ?: return@forEach
                val point = ChartPoint(timeLabel, value)
                val history = updated[chart.id] ?: emptyList()
                updated[chart.id] = (history + point).let { if (it.size > 24) it.takeLast(24) else it }
            }
            updated
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
            if (
                telemetry.linkState == LinkState.Online ||
                telemetry.linkState == LinkState.Connected ||
                telemetry.linkState == LinkState.Connecting
            ) {
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

    private fun clearMonitorData() {
        viewModelScope.launch {
            vehicleRepository.clearMonitorHistory()
            customChartHistory.value = emptyMap()
        }
    }

    private fun sendRawCommand(raw: String) {
        viewModelScope.launch {
            vehicleRepository.sendRawCommand(raw)
        }
    }

    private fun addCustomChart(chart: CustomChartDef) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().customCharts
            preferencesRepository.saveCustomCharts(current + chart)
        }
    }

    private fun deleteCustomChart(chartId: String) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().customCharts
            preferencesRepository.saveCustomCharts(current.filter { it.id != chartId })
            customChartHistory.update { it - chartId }
        }
    }

    private fun updateCustomChart(chart: CustomChartDef) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().customCharts
            preferencesRepository.saveCustomCharts(current.map { if (it.id == chart.id) chart else it })
            customChartHistory.update { it - chart.id }
        }
    }

    private fun updateCustomCommand(command: CustomCommandDef, saveAsDefault: Boolean = false) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().customCommands
            val updated = current.filter { it.commandId != command.commandId } + command
            preferencesRepository.saveCustomCommands(updated)
            if (saveAsDefault) saveCustomizedCommandDefaults()
        }
    }

    private fun addQuickButton(button: QuickButtonDef, saveAsDefault: Boolean = false) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().quickButtons
            preferencesRepository.saveQuickButtons(current + button)
            if (saveAsDefault) saveCustomizedCommandDefaults()
        }
    }

    private fun deleteQuickButton(buttonId: String, saveAsDefault: Boolean = false) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().quickButtons
            preferencesRepository.saveQuickButtons(current.filter { it.id != buttonId })
            if (saveAsDefault) saveCustomizedCommandDefaults()
        }
    }

    private fun updateQuickButton(button: QuickButtonDef, saveAsDefault: Boolean = false) {
        viewModelScope.launch {
            val current = preferencesRepository.preferences.first().quickButtons
            preferencesRepository.saveQuickButtons(current.map { if (it.id == button.id) button else it })
            if (saveAsDefault) saveCustomizedCommandDefaults()
        }
    }

    private fun restoreDefaultCommands() {
        viewModelScope.launch {
            val customizedJson = preferencesRepository.getCustomizedCommandDefaults()
            if (customizedJson.isNotBlank()) {
                // Load from customized defaults
                val customized = parseCustomizedCommandDefaults(customizedJson)
                preferencesRepository.saveCustomCommands(customized.customCommands)
                preferencesRepository.saveQuickButtons(customized.quickButtons)
            } else {
                // No customized defaults, clear to original
                preferencesRepository.saveCustomCommands(emptyList())
                preferencesRepository.saveQuickButtons(emptyList())
            }
        }
    }

    private fun saveCustomizedCommandDefaults() {
        viewModelScope.launch {
            val preferences = preferencesRepository.preferences.first()
            val json = JSONObject().apply {
                put("customCommands", JSONArray().apply {
                    preferences.customCommands.forEach { cmd ->
                        put(JSONObject().apply {
                            put("commandId", cmd.commandId)
                            put("customLabel", cmd.customLabel)
                            put("customWireValue", cmd.customWireValue)
                        })
                    }
                })
                put("quickButtons", JSONArray().apply {
                    preferences.quickButtons.forEach { btn ->
                        put(JSONObject().apply {
                            put("id", btn.id)
                            put("label", btn.label)
                            put("wireValue", btn.wireValue)
                        })
                    }
                })
            }
            preferencesRepository.saveCustomizedCommandDefaults(json.toString())
        }
    }

    private data class CustomizedCommandDefaults(
        val customCommands: List<CustomCommandDef>,
        val quickButtons: List<QuickButtonDef>,
    )

    private fun parseCustomizedCommandDefaults(json: String): CustomizedCommandDefaults {
        return try {
            val obj = JSONObject(json)
            val customCommandsArray = obj.optJSONArray("customCommands")
            val customCommands = if (customCommandsArray != null) {
                (0 until customCommandsArray.length()).mapNotNull { i ->
                    val cmd = customCommandsArray.optJSONObject(i) ?: return@mapNotNull null
                    CustomCommandDef(
                        commandId = cmd.optString("commandId", ""),
                        customLabel = cmd.optString("customLabel", ""),
                        customWireValue = cmd.optString("customWireValue", ""),
                    )
                }
            } else emptyList()
            val quickButtonsArray = obj.optJSONArray("quickButtons")
            val quickButtons = if (quickButtonsArray != null) {
                (0 until quickButtonsArray.length()).mapNotNull { i ->
                    val btn = quickButtonsArray.optJSONObject(i) ?: return@mapNotNull null
                    QuickButtonDef(
                        id = btn.optString("id", ""),
                        label = btn.optString("label", ""),
                        wireValue = btn.optString("wireValue", ""),
                    )
                }
            } else emptyList()
            CustomizedCommandDefaults(customCommands, quickButtons)
        } catch (_: Exception) {
            CustomizedCommandDefaults(emptyList(), emptyList())
        }
    }

    private fun toggleBuiltInChartVisible(chartId: String) {
        builtInChartStates.update { current ->
            val state = current[chartId] ?: BuiltInChartState()
            current + (chartId to state.copy(visible = !state.visible))
        }
    }

    private fun updateBuiltInChart(chartId: String, name: String, fieldPath: String, saveAsDefault: Boolean = false) {
        builtInChartStates.update { current ->
            val state = current[chartId] ?: BuiltInChartState()
            current + (chartId to state.copy(customName = name.ifBlank { null }, customFieldPath = fieldPath.ifBlank { null }))
        }
        if (saveAsDefault) saveCustomizedChartDefaults()
    }

    private fun deleteBuiltInChart(chartId: String, saveAsDefault: Boolean = false) {
        builtInChartStates.update { current ->
            current + (chartId to (current[chartId] ?: BuiltInChartState()).copy(deleted = true, visible = false))
        }
        if (saveAsDefault) saveCustomizedChartDefaults()
    }

    private fun restoreDefaultCharts() {
        viewModelScope.launch {
            val customizedJson = preferencesRepository.getCustomizedChartDefaults()
            if (customizedJson.isNotBlank()) {
                // Load from customized defaults
                val customized = parseCustomizedChartDefaults(customizedJson)
                builtInChartStates.value = customized
            } else {
                // No customized defaults, clear to original
                builtInChartStates.value = emptyMap()
            }
        }
    }

    private fun saveCustomizedChartDefaults() {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("chartStates", JSONObject().apply {
                    builtInChartStates.value.forEach { (id, state) ->
                        put(id, JSONObject().apply {
                            put("customName", state.customName ?: "")
                            put("customFieldPath", state.customFieldPath ?: "")
                            put("visible", state.visible)
                            put("deleted", state.deleted)
                        })
                    }
                })
            }
            preferencesRepository.saveCustomizedChartDefaults(json.toString())
        }
    }

    private fun parseCustomizedChartDefaults(json: String): Map<String, BuiltInChartState> {
        return try {
            val obj = JSONObject(json)
            val chartStates = obj.optJSONObject("chartStates") ?: return emptyMap()
            val result = mutableMapOf<String, BuiltInChartState>()
            val keys = chartStates.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val stateObj = chartStates.optJSONObject(id) ?: continue
                result[id] = BuiltInChartState(
                    customName = stateObj.optString("customName", "").ifBlank { null },
                    customFieldPath = stateObj.optString("customFieldPath", "").ifBlank { null },
                    visible = stateObj.optBoolean("visible", true),
                    deleted = stateObj.optBoolean("deleted", false),
                )
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun deleteOverviewItem(itemId: String, saveAsDefault: Boolean = false) {
        overviewItemStates.update { current ->
            current + (itemId to (current[itemId] ?: OverviewItemState(itemId)).copy(deleted = true))
        }
        if (saveAsDefault) saveCustomizedOverviewDefaults()
    }

    private fun editOverviewItem(itemId: String, newTitle: String, fieldPath: String, saveAsDefault: Boolean = false) {
        overviewItemStates.update { current ->
            current + (itemId to (current[itemId] ?: OverviewItemState(itemId)).copy(
                customTitle = newTitle.ifBlank { null },
                customFieldPath = fieldPath.ifBlank { null },
            ))
        }
        if (saveAsDefault) saveCustomizedOverviewDefaults()
    }

    private fun restoreDefaultOverview() {
        viewModelScope.launch {
            val customizedJson = preferencesRepository.getCustomizedOverviewDefaults()
            if (customizedJson.isNotBlank()) {
                // Load from customized defaults
                val customized = parseCustomizedOverviewDefaults(customizedJson)
                overviewItemStates.value = customized
            } else {
                // No customized defaults, clear to original
                overviewItemStates.value = emptyMap()
            }
        }
    }

    private fun saveCustomizedOverviewDefaults() {
        viewModelScope.launch {
            val json = JSONObject().apply {
                put("itemStates", JSONObject().apply {
                    overviewItemStates.value.forEach { (id, state) ->
                        put(id, JSONObject().apply {
                            put("customTitle", state.customTitle ?: "")
                            put("customFieldPath", state.customFieldPath ?: "")
                            put("deleted", state.deleted)
                        })
                    }
                })
            }
            preferencesRepository.saveCustomizedOverviewDefaults(json.toString())
        }
    }

    private fun parseCustomizedOverviewDefaults(json: String): Map<String, OverviewItemState> {
        return try {
            val obj = JSONObject(json)
            val itemStates = obj.optJSONObject("itemStates") ?: return emptyMap()
            val result = mutableMapOf<String, OverviewItemState>()
            val keys = itemStates.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                val stateObj = itemStates.optJSONObject(id) ?: continue
                result[id] = OverviewItemState(
                    id = id,
                    customTitle = stateObj.optString("customTitle", "").ifBlank { null },
                    customFieldPath = stateObj.optString("customFieldPath", "").ifBlank { null },
                    deleted = stateObj.optBoolean("deleted", false),
                )
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun resetOverviewData() {
        viewModelScope.launch {
            vehicleRepository.resetTelemetry()
            vehicleRepository.clearMonitorHistory()
            customChartHistory.value = emptyMap()
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

    private fun setColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            preferencesRepository.setColorTheme(theme)
        }
    }

    private fun setContrastLevel(level: Float) {
        viewModelScope.launch {
            preferencesRepository.setContrastLevel(level)
        }
    }

    private fun parseJsonFieldPath(json: JSONObject, path: String): Float? {
        if (path.isBlank()) return null
        val parts = path.split(".")
        var current: Any? = json
        for (part in parts) {
            val (key, index) = parsePart(part)
            current = when (current) {
                is JSONObject -> current.opt(key)
                else -> return null
            }
            if (index != null) {
                val array = runCatching { org.json.JSONArray(current.toString()) }.getOrNull() ?: return null
                current = array.opt(index)
            }
        }
        return when (current) {
            is Number -> current.toFloat()
            is String -> current.toFloatOrNull()
            else -> null
        }
    }

    private fun parsePart(part: String): Pair<String, Int?> {
        val bracketStart = part.indexOf('[')
        if (bracketStart < 0) return part to null
        val key = part.substring(0, bracketStart)
        val index = part.substring(bracketStart + 1).removeSuffix("]").toIntOrNull()
        return key to index
    }

    companion object {
        val DEFAULT_BUILT_IN_CHARTS = listOf(
            BuiltInChartDef("mlx_obj", "红外目标温度", "MLX90614 红外传感器目标温度", "MLX_obj"),
            BuiltInChartDef("aht_temp", "AHT20温度", "AHT20 温湿度传感器环境温度", "AHT_temp"),
            BuiltInChartDef("aht_hum", "AHT20湿度", "AHT20 温湿度传感器相对湿度", "AHT_hum"),
            BuiltInChartDef("mq8", "MQ8氢气", "MQ8 氢气传感器 ADC 原始值", "MQ8"),
            BuiltInChartDef("track", "四路循迹状态", "四路循迹传感器二进制状态热力图", "track_bin"),
            BuiltInChartDef("rfid_flow", "RFID 位置流程", "RFID 点位识别流程", "rfid"),
        )
    }
}
