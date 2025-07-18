package com.example.toolsapp.model.classes

import androidx.compose.ui.graphics.Color
import com.example.toolsapp.R

enum class ToolsThemeTypes{
    SPACE,
    POOL,
    GARDEN,
    MUD
}

sealed class Theme(
    val backgroundTexture: Int,
    val itemsTexture: Int,
    val recommendedColor: Color
){
    data object Space: Theme(
        backgroundTexture = R.drawable.space_texture,
        itemsTexture = R.drawable.star,
        recommendedColor = Color.White
    )

    data object Pool: Theme(
        backgroundTexture = R.drawable.pool_texture,
        itemsTexture = R.drawable.beach_ball,
        recommendedColor = Color.Black
    )

    data object Garden: Theme(
        backgroundTexture = R.drawable.grass_texture,
        itemsTexture = R.drawable.flower_texture,
        recommendedColor = Color.White
    )

    data object Mud: Theme(
        backgroundTexture = R.drawable.mud_texture,
        itemsTexture = R.drawable.worm_texture,
        recommendedColor = Color.White
    )

    companion object {
        fun getTheme(theme: ToolsThemeTypes): Theme {
            return when (theme) {
                ToolsThemeTypes.SPACE -> Space
                ToolsThemeTypes.POOL -> Pool
                ToolsThemeTypes.GARDEN -> Garden
                ToolsThemeTypes.MUD -> Mud
            }
        }

        fun getType(theme: Theme): ToolsThemeTypes {
            return when (theme) {
                Space -> ToolsThemeTypes.SPACE
                Pool -> ToolsThemeTypes.POOL
                Garden -> ToolsThemeTypes.GARDEN
                Mud -> ToolsThemeTypes.MUD
            }
        }
    }
}