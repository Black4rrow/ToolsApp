package com.example.toolsapp.model.classes

import com.example.toolsapp.viewModels.PreferencesViewModel
import com.google.firebase.auth.FirebaseAuth


object AppSettingsManager {
    var settings: Settings = Settings()
        private set

    private val preferencesViewModel = PreferencesViewModel()

    fun updateSelectedTheme(theme: ToolsThemeTypes) {settings.selectedTheme = theme}
    fun updateUserId(userId: String){settings.userId = userId}

    fun saveSettings() {
        updateUserId(FirebaseAuth.getInstance().currentUser!!.uid)
        preferencesViewModel.saveSettingsToDatabase(settings.userId, settings)
    }

    fun loadSettings(userId: String, onLoaded: () -> Unit) {
        preferencesViewModel.loadSettingsFromDatabase(userId) { loadedSettings ->
            settings = loadedSettings
            onLoaded()
        }
    }

    fun createNewSettings(){
        settings = Settings()
    }
}

class Settings(
    var userId: String = "",
    var selectedTheme: ToolsThemeTypes = ToolsThemeTypes.POOL
) {
}