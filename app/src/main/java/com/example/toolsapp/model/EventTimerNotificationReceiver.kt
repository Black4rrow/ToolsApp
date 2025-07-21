package com.example.toolsapp.model

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
        if (eventTimer != null) {
            val rescheduleIntent = Intent(context, RescheduleReceiver::class.java).apply {
                putExtra("eventTimer", eventTimer)
            }
            context.sendBroadcast(rescheduleIntent)
        }

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