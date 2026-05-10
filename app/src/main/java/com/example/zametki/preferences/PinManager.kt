package com.example.zametki.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PinManager(private val context: Context) {

    companion object {
        val PIN_KEY = stringPreferencesKey("pin_code")
    }

    val pin: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PIN_KEY] ?: ""
    }

    suspend fun setPin(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PIN_KEY] = pin
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit { preferences ->
            preferences.remove(PIN_KEY)
        }
    }
}