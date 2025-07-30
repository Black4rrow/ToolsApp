package com.example.toolsapp.model

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.toolsapp.R
import com.example.toolsapp.viewModels.repositories.EventTimerRepository

class EventTimerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "event_timer_channel"
        val channelName = "Event Timer Notifications"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val eventTimerTitle = intent.getStringExtra("eventTimerTitle")
        val eventTimer:EventTimer = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("eventTimer", EventTimer::class.java)
        }else{
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("eventTimer")
        } ?: return
        val rescheduleIntent = Intent(context, RescheduleReceiver::class.java).apply {
            putExtra("eventTimer", eventTimer)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            eventTimer.id + 1000,
            rescheduleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                try {
                    val myIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(myIntent)
                } catch (e: ActivityNotFoundException) {
                    Log.w("AlarmSetup", "No settings activity to request exact alarms.")
                }
                return
            }
        }
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 1000,
            pendingIntent
        )
//        context.sendBroadcast(rescheduleIntent)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.event_alarm))
            .setContentText(eventTimerTitle + " " + context.getString(R.string.is_finished))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(0, notificationBuilder.build())
    }
}


class RescheduleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventTimerRepository = EventTimerRepository()
        val eventTimer:EventTimer = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("eventTimer", EventTimer::class.java)
        }else{
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("eventTimer")
        } ?: return

        if (eventTimer.loopMode == "NONE") return

        val newEndDate = eventTimer.calculateNextEndDate()
        val updatedEventTimer = eventTimer.copy(endDate = newEndDate)

        eventTimerRepository.updateEventTimer(updatedEventTimer)
        eventTimerRepository.createEventTimerNotification(context, updatedEventTimer)
    }
}