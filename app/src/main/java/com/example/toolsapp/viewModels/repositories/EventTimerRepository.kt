package com.example.toolsapp.viewModels.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.toolsapp.model.EventTimer
import com.example.toolsapp.model.EventTimerNotificationReceiver
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class EventTimerRepository {
    private val database: FirebaseDatabase = Firebase.database
    private val myRef = database.getReference("evenTimers")

    private val _aTimerWasUpdated = MutableStateFlow(false)
    val aTimerWasUpdated: StateFlow<Boolean> = _aTimerWasUpdated

    fun addEventTimer(eventTimer: EventTimer) {
        val itemId = eventTimer.id.toString()
        itemId.let {
            myRef.child(it).setValue(eventTimer)
        }
        _aTimerWasUpdated.value = true
    }

    fun resetUpdateFlag() {
        _aTimerWasUpdated.value = false
    }

    fun getEventTimers(userId: String, onDataChange: (List<EventTimer>) -> Unit) {
        myRef.orderByChild("userId").equalTo(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val eventTimers = mutableListOf<EventTimer>()
                for (itemSnapshot in snapshot.children) {
                    val todoItem = itemSnapshot.getValue(EventTimer::class.java)
                    todoItem?.let {
                        eventTimers.add(it)
                    }
                }
                val sortedEventTimers = eventTimers.sortedBy { it.endDate }
                val (positiveRemainingTime, nonPositiveRemainingTime) = sortedEventTimers.partition { it.getRemainingTime() > 0 }
                val finalSortedEventTimers = positiveRemainingTime + nonPositiveRemainingTime

                onDataChange(finalSortedEventTimers)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun updateEventTimer(evenTimer: EventTimer) {
        val itemId = evenTimer.id.toString()
        myRef.child(itemId).setValue(evenTimer)
        _aTimerWasUpdated.value = true
    }

    fun deleteEventTimer(eventTimerId: String) {
        myRef.child(eventTimerId).removeValue()
        _aTimerWasUpdated.value = true
    }

    fun createEventTimerNotification(context: Context, eventTimer: EventTimer) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        val intent = Intent(context, EventTimerNotificationReceiver::class.java)
        intent.putExtra("eventTimerTitle", eventTimer.title)
        intent.putExtra("eventTimer", eventTimer)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventTimer.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = dateFormat.parse(eventTimer.endDate)

        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, eventTimer.endHour.toInt())
            set(Calendar.MINUTE, eventTimer.endMinute.toInt())
            set(Calendar.SECOND, 0)
            timeZone = TimeZone.getDefault()
        }

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
