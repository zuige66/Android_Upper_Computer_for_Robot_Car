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
import com.carhost.mobile.data.model.OperatorPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
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

    private object Keys {
        val Host = stringPreferencesKey("host")
        val Port = intPreferencesKey("port")
        val DynamicColor = booleanPreferencesKey("dynamic_color")
        val KeepScreenOn = booleanPreferencesKey("keep_screen_on")
        val Notifications = booleanPreferencesKey("notifications_enabled")
    }
}
