package com.example.toolsapp.model


data class TodoItem (
    val id: Int = 0,
    val title: String = "Unnamed",
    val description: String = "",
    var isFinished: Boolean = false,
    val endDate: String = "",
    val userId: String = ""
){
    override fun toString(): String {
        return "TodoItem(title='$title', description='$description', isFinished=$isFinished, endDate='$endDate')"
    }
}