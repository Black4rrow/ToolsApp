package com.example.toolsapp.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object Utils {
    private val _connectedToInternet = MutableStateFlow(false)
    val connectedToInternet: StateFlow<Boolean> = _connectedToInternet

    fun setConnectedToInternet(isConnected: Boolean) {
        _connectedToInternet.value = isConnected
    }
}