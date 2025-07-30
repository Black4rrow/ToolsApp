package com.example.toolsapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.toolsapp.R
import com.example.toolsapp.model.classes.AppSettingsManager
import com.example.toolsapp.model.classes.Theme
import com.example.toolsapp.model.classes.ToolsThemeTypes
import com.example.toolsapp.ui.components.CategorySpacer
import com.example.toolsapp.ui.components.ColorSchemePicker
import com.example.toolsapp.ui.theme.AllColorSchemes

@Composable
fun SettingsScreen() {
    val allThemesTypes: List<ToolsThemeTypes> = ToolsThemeTypes.entries
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedTheme by remember {
        mutableStateOf(Theme.getTheme(AppSettingsManager.settings.selectedTheme))
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {

            CategorySpacer(stringResource(R.string.choose_theme), 0)

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            ) {
                items(allThemesTypes.size) { index ->
                    val theme: Theme = Theme.getTheme(allThemesTypes[index])
                    val imageBitmap = ImageBitmap.imageResource(theme.backgroundTexture)
                    val backgroundBrush = remember(imageBitmap) {
                        ShaderBrush(ImageShader(imageBitmap, TileMode.Repeated, TileMode.Repeated))
                    }

                    val isSelected = selectedTheme == theme

                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .background(backgroundBrush)
                            .clickable {
                                selectedTheme = theme
                                AppSettingsManager.updateSelectedTheme(Theme.getType(theme))
                            }
                            .then(
                                if (isSelected) {
                                    Modifier.border(2.dp, Color.Red)
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        // Contenu de la carte
                    }
                }
            }

            CategorySpacer(stringResource(R.string.color_scheme_choice))

            val selectedColorScheme by AppSettingsManager.settings.selectedColorScheme.collectAsState()

            for (key in AllColorSchemes.lightSchemes.keys) {
                val lightList = AllColorSchemes.lightSchemes[key]
                val darkList = AllColorSchemes.darkSchemes[key]

                if (lightList != null && darkList != null) {
                    ColorSchemePicker(
                        lightColorSchemes = lightList,
                        darkColorSchemes = darkList,
                        modifier = Modifier
                            .fillMaxWidth(),
                        onColorSchemeSelected = {
                            AppSettingsManager.settings.selectedColorScheme.value = AllColorSchemes.getNameFromScheme(it)
                        }
                    )
                }
            }
        }
    }
}