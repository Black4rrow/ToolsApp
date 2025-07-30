package com.example.toolsapp.model.classes

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.toolsapp.ui.theme.AllColorSchemes
import com.example.toolsapp.viewModels.PreferencesViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow


object AppSettingsManager {
    var settings: Settings = Settings()
        private set

    private val preferencesViewModel = PreferencesViewModel()

    fun updateSelectedTheme(theme: ToolsThemeTypes) {settings.selectedTheme = theme}
    fun updateUserId(userId: String){settings.userId = userId}

    fun saveSettings() {
        updateUserId(FirebaseAuth.getInstance().currentUser!!.uid)
        preferencesViewModel.saveSettingsToDatabase(settings.userId, settings.toPersistable())
    }

    fun loadSettings(userId: String, onLoaded: () -> Unit) {
        preferencesViewModel.loadSettingsFromDatabase(userId) { loadedSettings ->
            settings.updateFromPersistable(loadedSettings)
            onLoaded()
        }
    }

    fun createNewSettings(){
        settings = Settings()
    }
}

class Settings(
    var userId: String = "",
    var selectedTheme: ToolsThemeTypes = ToolsThemeTypes.POOL,
    var selectedColorScheme: MutableStateFlow<String> = MutableStateFlow(AllColorSchemes.getNameFromScheme()),
    var selectedContrast: MutableStateFlow<Int> = MutableStateFlow(0),
    var selectedIsDark: MutableStateFlow<Boolean> = MutableStateFlow(false)
){
    fun toPersistable(): PersistableSettings {
        return PersistableSettings(
            userId = userId,
            selectedTheme = selectedTheme,
            selectedColorScheme = selectedColorScheme.value,
            selectedContrast = selectedContrast.value,
            selectedIsDark = selectedIsDark.value
        )
    }

    fun updateFromPersistable(persistable: PersistableSettings) {
        userId = persistable.userId
        selectedTheme = persistable.selectedTheme
        selectedColorScheme.value = persistable.selectedColorScheme
        selectedContrast.value = persistable.selectedContrast
        selectedIsDark.value = persistable.selectedIsDark
    }
}

data class PersistableSettings(
    val userId: String = "",
    val selectedTheme: ToolsThemeTypes = ToolsThemeTypes.POOL,
    val selectedColorScheme: String = AllColorSchemes.getNameFromScheme(),
    val selectedContrast: Int = 0,
    val selectedIsDark: Boolean = false
)