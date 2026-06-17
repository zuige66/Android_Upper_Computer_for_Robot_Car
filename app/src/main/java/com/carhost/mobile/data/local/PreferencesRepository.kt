package com.carhost.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.carhost.mobile.data.model.ConnectionProfile
import com.carhost.mobile.data.model.CustomChartDef
import com.carhost.mobile.data.model.CustomChartType
import com.carhost.mobile.data.model.CustomCommandDef
import com.carhost.mobile.data.model.OperatorPreferences
import com.carhost.mobile.data.model.QuickButtonDef
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("operator_preferences.preferences_pb") }
    )

    val preferences: Flow<OperatorPreferences> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { prefs ->
            OperatorPreferences(
                endpoint = ConnectionProfile(
                    host = prefs[Keys.Host] ?: ConnectionProfile().host,
                    port = prefs[Keys.Port] ?: ConnectionProfile().port,
                ),
                useDynamicColor = prefs[Keys.DynamicColor] ?: true,
                keepScreenOn = prefs[Keys.KeepScreenOn] ?: true,
                notificationsEnabled = prefs[Keys.Notifications] ?: false,
                customCharts = parseCustomCharts(prefs[Keys.CustomCharts] ?: "[]"),
                customCommands = parseCustomCommands(prefs[Keys.CustomCommands] ?: "[]"),
                quickButtons = parseQuickButtons(prefs[Keys.QuickButtons] ?: "[]"),
            )
        }

    suspend fun saveEndpoint(host: String, port: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.Host] = host
            prefs[Keys.Port] = port
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DynamicColor] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.KeepScreenOn] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.Notifications] = enabled }
    }

    suspend fun saveCustomCharts(charts: List<CustomChartDef>) {
        val jsonArray = JSONArray()
        charts.forEach { chart ->
            jsonArray.put(JSONObject().apply {
                put("id", chart.id)
                put("name", chart.name)
                put("fieldPath", chart.fieldPath)
                put("unit", chart.unit)
                put("type", chart.type.name)
            })
        }
        dataStore.edit { prefs -> prefs[Keys.CustomCharts] = jsonArray.toString() }
    }

    suspend fun saveCustomCommands(commands: List<CustomCommandDef>) {
        val jsonArray = JSONArray()
        commands.forEach { cmd ->
            jsonArray.put(JSONObject().apply {
                put("commandId", cmd.commandId)
                put("customLabel", cmd.customLabel)
                put("customWireValue", cmd.customWireValue)
            })
        }
        dataStore.edit { prefs -> prefs[Keys.CustomCommands] = jsonArray.toString() }
    }

    suspend fun saveQuickButtons(buttons: List<QuickButtonDef>) {
        val jsonArray = JSONArray()
        buttons.forEach { btn ->
            jsonArray.put(JSONObject().apply {
                put("id", btn.id)
                put("label", btn.label)
                put("wireValue", btn.wireValue)
            })
        }
        dataStore.edit { prefs -> prefs[Keys.QuickButtons] = jsonArray.toString() }
    }

    private fun parseCustomCharts(json: String): List<CustomChartDef> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                CustomChartDef(
                    id = obj.optString("id", ""),
                    name = obj.optString("name", ""),
                    fieldPath = obj.optString("fieldPath", ""),
                    unit = obj.optString("unit", ""),
                    type = try { CustomChartType.valueOf(obj.optString("type", "Line")) } catch (_: Exception) { CustomChartType.Line },
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseCustomCommands(json: String): List<CustomCommandDef> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                CustomCommandDef(
                    commandId = obj.optString("commandId", ""),
                    customLabel = obj.optString("customLabel", ""),
                    customWireValue = obj.optString("customWireValue", ""),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun parseQuickButtons(json: String): List<QuickButtonDef> {
        return try {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                QuickButtonDef(
                    id = obj.optString("id", ""),
                    label = obj.optString("label", ""),
                    wireValue = obj.optString("wireValue", ""),
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private object Keys {
        val Host = stringPreferencesKey("host")
        val Port = intPreferencesKey("port")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
        val KeepScreenOn = booleanPreferencesKey("keep_screen_on")
        val Notifications = booleanPreferencesKey("notifications_enabled")
        val CustomCharts = stringPreferencesKey("custom_charts")
        val CustomCommands = stringPreferencesKey("custom_commands")
        val QuickButtons = stringPreferencesKey("quick_buttons")
    }
}
