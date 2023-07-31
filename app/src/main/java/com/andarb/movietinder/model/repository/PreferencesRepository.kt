package com.andarb.movietinder.model.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.andarb.movietinder.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import kotlin.random.Random

/**
 * Stores user settings
 */
data class UserPreferences(val deviceName: String, val movieCount: Int)

/**
 * Modifies and retrieves user settings
 */
class PreferencesRepository(private val dataStore: DataStore<Preferences>, context: Context) {
    private object PreferencesKeys {
        val DEVICE_NAME = stringPreferencesKey("device_name")
        val MOVIE_COUNT = intPreferencesKey("movie_count")
    }

    private val defaultName: String =
        context.getString(R.string.default_device_name) + Random.nextInt(100, 999).toString()
    private val defaultNumber = 5

    val preferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            val deviceName = preferences[PreferencesKeys.DEVICE_NAME] ?: defaultName
            val movieCount = preferences[PreferencesKeys.MOVIE_COUNT] ?: defaultNumber
            UserPreferences(deviceName, movieCount)
        }

    suspend fun updateDeviceName(deviceName: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEVICE_NAME] = deviceName
        }
    }

    suspend fun updateMovieCount(movieCount: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.MOVIE_COUNT] = movieCount
        }
    }
}