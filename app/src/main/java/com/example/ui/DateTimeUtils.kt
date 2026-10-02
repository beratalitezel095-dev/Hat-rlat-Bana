package com.example.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    private val turkishLocale = Locale("tr", "TR")

    fun formatDateTime(timeMillis: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timeMillis }

        val timeFormat = SimpleDateFormat("HH:mm", turkishLocale)
        val timeString = timeFormat.format(Date(timeMillis))

        val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = tomorrow.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                tomorrow.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        return when {
            isSameDay -> "Bugün, $timeString"
            isTomorrow -> "Yarın, $timeString"
            isYesterday -> "Dün, $timeString"
            now.get(Calendar.YEAR) == target.get(Calendar.YEAR) -> {
                val dateFormat = SimpleDateFormat("d MMMM, HH:mm", turkishLocale)
                dateFormat.format(Date(timeMillis))
            }
            else -> {
                val fullFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", turkishLocale)
                fullFormat.format(Date(timeMillis))
            }
        }
    }

    fun getRemainingTimeText(timeMillis: Long): String {
        val diff = timeMillis - System.currentTimeMillis()
        if (diff <= 0) {
            val passedMinutes = (-diff / (60 * 1000)).toInt()
            return when {
                passedMinutes < 1 -> "Şimdi"
                passedMinutes < 60 -> "$passedMinutes dk geçti"
                passedMinutes < 1440 -> "${passedMinutes / 60} sa geçti"
                else -> "${passedMinutes / 1440} gün geçti"
            }
        }

        val minutes = (diff / (60 * 1000)).toInt()
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "1 dakikadan az"
            minutes < 60 -> "$minutes dk kaldı"
            hours < 24 -> {
                val remainingMin = minutes % 60
                if (remainingMin > 0) "$hours sa $remainingMin dk kaldı" else "$hours saat kaldı"
            }
            else -> "$days gün kaldı"
        }
    }
}
