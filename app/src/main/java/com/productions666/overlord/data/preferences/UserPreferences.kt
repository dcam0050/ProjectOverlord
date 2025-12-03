package com.productions666.overlord.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Available font options for the app
 */
enum class AppFont(
    val displayName: String,
    val description: String
) {
    LEXEND(
        displayName = "Lexend",
        description = "Designed for reading fluency"
    ),
    OPEN_DYSLEXIC(
        displayName = "OpenDyslexic",
        description = "Designed to help with dyslexia"
    ),
    SYSTEM_DEFAULT(
        displayName = "System Default",
        description = "Uses your device's default font"
    )
}

/**
 * User preferences stored in DataStore
 */
data class UserPreferencesData(
    val selectedFont: AppFont = AppFont.LEXEND,
    val homeAddress: String? = null,
    val workAddress: String? = null
)

// Extension to create DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Repository for managing user preferences
 */
class UserPreferencesRepository(private val context: Context) {
    
    private object PreferencesKeys {
        val SELECTED_FONT = stringPreferencesKey("selected_font")
        val HOME_ADDRESS = stringPreferencesKey("home_address")
        val WORK_ADDRESS = stringPreferencesKey("work_address")
    }
    
    /**
     * Flow of user preferences
     */
    val preferencesFlow: Flow<UserPreferencesData> = context.dataStore.data.map { preferences ->
        val fontName = preferences[PreferencesKeys.SELECTED_FONT] ?: AppFont.LEXEND.name
        val font = try {
            AppFont.valueOf(fontName)
        } catch (e: IllegalArgumentException) {
            AppFont.LEXEND
        }
        
        UserPreferencesData(
            selectedFont = font,
            homeAddress = preferences[PreferencesKeys.HOME_ADDRESS],
            workAddress = preferences[PreferencesKeys.WORK_ADDRESS]
        )
    }
    
    /**
     * Flow of just the selected font
     */
    val selectedFontFlow: Flow<AppFont> = preferencesFlow.map { it.selectedFont }
    
    /**
     * Update the selected font
     */
    suspend fun setSelectedFont(font: AppFont) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_FONT] = font.name
        }
    }
    
    /**
     * Update home address
     */
    suspend fun setHomeAddress(address: String?) {
        context.dataStore.edit { preferences ->
            if (address != null) {
                preferences[PreferencesKeys.HOME_ADDRESS] = address
            } else {
                preferences.remove(PreferencesKeys.HOME_ADDRESS)
            }
        }
    }
    
    /**
     * Update work address
     */
    suspend fun setWorkAddress(address: String?) {
        context.dataStore.edit { preferences ->
            if (address != null) {
                preferences[PreferencesKeys.WORK_ADDRESS] = address
            } else {
                preferences.remove(PreferencesKeys.WORK_ADDRESS)
            }
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null
        
        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreferencesRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

