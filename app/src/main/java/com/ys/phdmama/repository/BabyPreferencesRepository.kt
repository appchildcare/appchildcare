package com.ys.phdmama.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class BabyPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val SELECTED_BABY_ID = stringPreferencesKey("selected_baby_id")
    }

    // Save selected baby ID
    suspend fun saveSelectedBabyId(babyId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_BABY_ID] = babyId
            Log.d("BabyPreferences", "Saved baby ID: $babyId")
        }
    }

    // Get selected baby ID as Flow (reactive)
    val selectedBabyIdFlow: Flow<String?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_BABY_ID]
        }

    // Get selected baby ID once (one-time read)
    suspend fun getSelectedBabyId(): String? {
        return dataStore.data.firstOrNull()?.get(PreferencesKeys.SELECTED_BABY_ID)
    }

    // Clear selected baby ID
    suspend fun clearSelectedBabyId() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SELECTED_BABY_ID)
            Log.d("BabyPreferences", "Cleared baby ID")
        }
    }
}
