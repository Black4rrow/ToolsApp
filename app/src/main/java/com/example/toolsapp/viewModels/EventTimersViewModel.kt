package com.example.toolsapp.viewModels

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.toolsapp.model.EventTimer
import com.example.toolsapp.model.EventTimerNotificationReceiver
import com.example.toolsapp.viewModels.repositories.EventTimerRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


class EventTimersViewModel : ViewModel(){
    private val eventTimerRepository = EventTimerRepository()
    val aTimerWasUpdated = eventTimerRepository.aTimerWasUpdated.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun resetUpdateFlag() {
        eventTimerRepository.resetUpdateFlag()
    }

    fun addEventTimer(eventTimer: EventTimer) {
        eventTimerRepository.addEventTimer(eventTimer)
    }

    fun getEventTimers(userId: String, onDataChange: (List<EventTimer>) -> Unit) {
        eventTimerRepository.getEventTimers(userId, onDataChange)
    }

    fun updateEventTimer(evenTimer: EventTimer) {
        eventTimerRepository.updateEventTimer(evenTimer)
    }

    fun deleteEventTimer(eventTimerId: String) {
        eventTimerRepository.deleteEventTimer(eventTimerId)
    }

    fun createEventTimerNotification(context: Context, eventTimer: EventTimer) {
        eventTimerRepository.createEventTimerNotification(context, eventTimer)
    }
}