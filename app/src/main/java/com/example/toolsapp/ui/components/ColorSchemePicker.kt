package com.example.toolsapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.toolsapp.R
import com.example.toolsapp.model.classes.AppSettingsManager
import com.example.toolsapp.model.leftBorder
import com.example.toolsapp.model.rightBorder
import com.example.toolsapp.ui.theme.AllColorSchemes
import com.example.toolsapp.ui.theme.lightHighlightColor
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

@Composable
fun ColorSchemePicker(
    lightColorSchemes: List<ColorScheme>,
    darkColorSchemes: List<ColorScheme>,
    modifier: Modifier = Modifier,
    onColorSchemeSelected: (ColorScheme) -> Unit
){
    val selectedColorScheme by AppSettingsManager.settings.selectedColorScheme.collectAsState()
    var currentContrast by remember { mutableIntStateOf(0) }
    var isDark by remember { mutableStateOf(false) }


    val isSelected = remember {
        derivedStateOf {
            AllColorSchemes.getSchemeFromName(selectedColorScheme) in lightColorSchemes ||
                    AllColorSchemes.getSchemeFromName(selectedColorScheme) in darkColorSchemes
        }
    }

    if(isSelected.value){
        currentContrast = AppSettingsManager.settings.selectedContrast.collectAsState().value
        isDark = AppSettingsManager.settings.selectedIsDark.collectAsState().value
    }
    val options = listOf("WEAK", "MEDIUM", "HIGH")
    val schemeToDisplay = if (isDark) darkColorSchemes[currentContrast] else lightColorSchemes[currentContrast]


    Card(modifier = modifier.padding(8.dp)){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.75f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .clickable {
                            onColorSchemeSelected(
                                if (isDark) darkColorSchemes[currentContrast] else lightColorSchemes[currentContrast]
                            )
                        }) {
                    Row {
                        Box(Modifier.background(schemeToDisplay.primary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.onPrimary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.primaryContainer).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.onPrimaryContainer).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.secondary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.onSecondary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.secondaryContainer).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.tertiary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.onTertiary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.tertiaryContainer).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.onBackground).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.surface).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.inversePrimary).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.inverseSurface).fillMaxHeight().weight(1f))
                        Box(Modifier.background(schemeToDisplay.errorContainer).fillMaxHeight().weight(1f))
                    }
                }

                Text(modifier = Modifier.fillMaxWidth().background(schemeToDisplay.surfaceContainer),
                    text = stringResource(R.string.contrast),
                    style = MaterialTheme.typography.titleMedium.copy(),
                    textAlign = TextAlign.Center,
                    color = schemeToDisplay.onSurface
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.height(32.dp)
                ){
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            modifier = Modifier
                                .padding(0.dp),
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size,
                                baseShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                            ),
                            onClick = {
                                if(!isSelected.value){
                                    onColorSchemeSelected(
                                        if (isDark) darkColorSchemes[currentContrast] else lightColorSchemes[currentContrast]
                                    )
                                }
                                currentContrast = index
                                AppSettingsManager.settings.selectedContrast.value = index
                            },
                            selected = index == currentContrast,
                            label = {
                                val style = MaterialTheme.typography.labelSmall
                                when (label) {
                                    "WEAK" -> Text(stringResource(R.string.weak), style = style)
                                    "MEDIUM" -> Text(stringResource(R.string.medium), style = style)
                                    "HIGH" -> Text(stringResource(R.string.high), style = style)
                                }
                            }
                        )
                    }
                }
            }

            Column(modifier = Modifier
                .weight(0.25f)){
                Text(stringResource(R.string.dark))
                Checkbox(
                    modifier = Modifier,
                    checked = isDark,
                    onCheckedChange = {
                        if (!isSelected.value) {
                            onColorSchemeSelected(
                                if (it) darkColorSchemes[currentContrast] else lightColorSchemes[currentContrast]
                            )
                        }
                        isDark = it
                        AppSettingsManager.settings.selectedIsDark.value = it
                    },
                )
            }
        }
    }
}