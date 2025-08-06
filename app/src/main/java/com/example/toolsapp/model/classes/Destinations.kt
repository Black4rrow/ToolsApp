package com.example.toolsapp.model.classes

import android.content.Context
import androidx.compose.runtime.Composable
import com.example.toolsapp.R

interface DestinationItem{
    val route: String
    var title: String
    val iconId: Int
    val titleRessourceId: Int
    val showBottomBar: Boolean

    fun getTitleTranslation(context: Context): String{
        return context.getString(titleRessourceId)
    }
}

sealed class Destination(override val route: String): DestinationItem {
    data object Profile : Destination("profile") {
        override var title: String = "Profil"
        override val iconId: Int = R.drawable.ic_launcher_foreground
        override val titleRessourceId: Int = R.string.profile_nav
        override val showBottomBar: Boolean = true
    }

    data object ToolsList : Destination("tools_list") {
        override var title: String = "Accueil"
        override val iconId: Int = R.drawable.ic_launcher_foreground
        override val titleRessourceId: Int = R.string.home_nav
        override val showBottomBar: Boolean = true
    }

    data object Settings : Destination("settings") {
        override var title: String = "Paramètres"
        override val iconId: Int = R.drawable.ic_launcher_foreground
        override val titleRessourceId: Int = R.string.settings_nav
        override val showBottomBar: Boolean = true
    }
}

sealed class ToolsDestination(
    override val route: String,
    override var title: String,
    override val titleRessourceId: Int = R.string.no_name,
    override val iconId: Int,
    val requireConnection: Boolean,
    override val showBottomBar: Boolean
) : DestinationItem {
    data object EventTimers : ToolsDestination("event_timers", "Event Timers",R.string.event_timer_title, R.drawable.timer, true, true)
    data object TodoList : ToolsDestination("todo", "Todo",R.string.todo_title, R.drawable.checklist, true, true)
    data object Particles : ToolsDestination("particles", "Particles",R.string.particles_title, R.drawable.particles, false, false)
    data object FrenzyClicker : ToolsDestination("frenzy_clicker", "FrenzyClicker", R.string.frenzy_clicker_title, R.drawable.frenzy_clicker, false, false)
    data object GridConqueror: ToolsDestination("grid_conqueror", "Grid Conqueror", R.string.grid_conqueror_title, R.drawable.grid_conqueror, false, false)

    companion object {
        val all by lazy { listOf(EventTimers, TodoList, Particles, FrenzyClicker, GridConqueror) }
    }
}