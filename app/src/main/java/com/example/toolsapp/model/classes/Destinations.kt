package com.example.toolsapp.model.classes

import com.example.toolsapp.R

interface DestinationItem{
    val route: String
    val title: String
    val iconId: Int
}

sealed class Destination(override val route: String): DestinationItem {
    data object Profile : Destination("profile") {
        override val title: String = "Profil"
        override val iconId: Int = R.drawable.ic_launcher_foreground
    }

    data object ToolsList : Destination("tools_list") {
        override val title: String = "Accueil"
        override val iconId: Int = R.drawable.ic_launcher_foreground
    }

    data object Settings : Destination("settings") {
        override val title: String = "Paramètres"
        override val iconId: Int = R.drawable.ic_launcher_foreground
    }
}

sealed class ToolsDestination(
    override val route: String,
    override val title: String,
    override val iconId: Int,
    val requireConnection: Boolean,
) : DestinationItem {
    data object EventTimers : ToolsDestination("event_timers", "Event Timers", R.drawable.timer, true)
    data object TodoList : ToolsDestination("todo", "Todo", R.drawable.checklist, true)
    data object Particles : ToolsDestination("particles", "Particles", R.drawable.drag_indicator, false)

    companion object {
        val all by lazy { listOf(EventTimers, TodoList, Particles) }
    }
}