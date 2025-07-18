package com.example.toolsapp.model.classes

import com.example.toolsapp.ui.viewModels.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth


object AppSettingsManager {
    var settings: Settings = Settings()
        private set

    private val settingsViewModel = SettingsViewModel()

    fun updateSelectedTheme(theme: ToolsThemeTypes) {settings.selectedTheme = theme}
    fun updateUserId(userId: String){settings.userId = userId}

    fun saveSettings() {
        updateUserId(FirebaseAuth.getInstance().currentUser!!.uid)
        settingsViewModel.saveToDatabase(settings.userId, settings)
    }

    fun loadSettings(userId: String, onLoaded: () -> Unit) {
        settingsViewModel.loadFromDatabase(userId) { loadedSettings ->
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