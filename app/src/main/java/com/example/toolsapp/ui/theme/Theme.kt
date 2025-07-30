package com.example.toolsapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.toolsapp.ui.theme.colors.ColorBlueish
import com.example.toolsapp.ui.theme.colors.ColorDarkish
import com.example.toolsapp.ui.theme.colors.ColorGreenish
import com.example.toolsapp.ui.theme.colors.ColorPinky
import com.example.toolsapp.ui.theme.colors.ColorYellowish

val lightHighlightColor = Color(0xFFFD1D1D)


object AllColorSchemes {
    val lightSchemes: Map<String, List<ColorScheme>> = mapOf(
        "greenish" to listOf(ColorGreenish.lightScheme, ColorGreenish.mediumContrastLightColorScheme, ColorGreenish.highContrastLightColorScheme),
        "pinky" to listOf(ColorPinky.lightScheme, ColorPinky.mediumContrastLightColorScheme, ColorPinky.highContrastLightColorScheme),
        "yellowish" to listOf(ColorYellowish.lightScheme, ColorYellowish.mediumContrastLightColorScheme, ColorYellowish.highContrastLightColorScheme),
        "blueish" to listOf(ColorBlueish.lightScheme, ColorBlueish.mediumContrastLightColorScheme, ColorBlueish.highContrastLightColorScheme),
        "darkish" to listOf(ColorDarkish.lightScheme, ColorDarkish.mediumContrastLightColorScheme, ColorDarkish.highContrastLightColorScheme),
    )

    val darkSchemes: Map<String, List<ColorScheme>> = mapOf(
        "greenish" to listOf(ColorGreenish.darkScheme, ColorGreenish.mediumContrastDarkColorScheme, ColorGreenish.highContrastDarkColorScheme),
        "pinky" to listOf(ColorPinky.darkScheme, ColorPinky.mediumContrastDarkColorScheme, ColorPinky.highContrastDarkColorScheme),
        "yellowish" to listOf(ColorYellowish.darkScheme, ColorYellowish.mediumContrastDarkColorScheme, ColorYellowish.highContrastDarkColorScheme),
        "blueish" to listOf(ColorBlueish.darkScheme, ColorBlueish.mediumContrastDarkColorScheme, ColorBlueish.highContrastDarkColorScheme),
        "darkish" to listOf(ColorDarkish.darkScheme, ColorDarkish.mediumContrastDarkColorScheme, ColorDarkish.highContrastDarkColorScheme),
    )

    fun getSchemeFromName(name: String = "greenish"): ColorScheme {
        return if (name in lightSchemes) {
            lightSchemes[name]!!.first()
        } else if(name in darkSchemes) {
            darkSchemes[name]!!.first()
        }else{
            lightSchemes.values.first().first()
        }
    }

    fun getSchemeFromNameAndContrastAndIsDark(name: String = "greenish", contrast: Int, isDark: Boolean): ColorScheme {
        return if(!isDark){
            lightSchemes[name]!![contrast]
        }else{
            darkSchemes[name]!![contrast]
        }
    }

    fun getNameFromScheme(scheme: ColorScheme? = null): String {
        return if (scheme == null) {
            lightSchemes.keys.first()
        }else if(scheme in lightSchemes.values.flatten()) {
            lightSchemes.keys.first { scheme in lightSchemes[it]!!
            }
        } else if (scheme in darkSchemes.values.flatten()) {
            darkSchemes.keys.first { scheme in darkSchemes[it]!! }
        } else {
            lightSchemes.keys.first()
        }
    }
}

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun ToolsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    customColorScheme: ColorScheme? = null,
    content: @Composable() () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ColorGreenish.darkScheme
        customColorScheme != null -> customColorScheme
        else -> ColorGreenish.lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}