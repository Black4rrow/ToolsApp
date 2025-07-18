package com.example.toolsapp.model

import java.text.SimpleDateFormat
import java.util.*

data class EventTimer(
    val id: Int = 0,
    var title: String = "Unnamed",
    var loopMode: String = "",
    var endDate: String = "",
    var endHour: String = "",
    var endMinute: String = "",
    val userId: String = ""
) {
    fun getRemainingTime(): Long {
        if (endDate.isEmpty() || endHour.isEmpty() || endMinute.isEmpty()) return 0L

        return try {
            val paddedHour = endHour.padStart(2, '0')
            val paddedMinute = endMinute.padStart(2, '0')

            val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).apply {
                timeZone = TimeZone.getDefault()
            }

            val endTime = parser.parse("$endDate $paddedHour:$paddedMinute") ?: return 0L

            maxOf(0L, endTime.time - System.currentTimeMillis())
        } catch (e: Exception) {
            android.util.Log.e("EventTimer", "Erreur de parsing", e)
            0L
        }
    }

    fun calculateNextEndDate(): String{
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentEnd = sdf.parse("$endDate $endHour:$endMinute") ?: return endDate

        val calendar = Calendar.getInstance().apply { time = currentEnd }

        when(loopMode){
            "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
            "YEARLY" -> calendar.add(Calendar.YEAR, 1)
        }

        return sdf.format(calendar.time).split(" ")[0]
    }
}