package com.example.data

import android.content.Context
import com.example.model.Reminder
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import java.io.File

class ReminderRepository(
    private val dao: ReminderDao,
    private val context: Context
) {

    val allReminders: Flow<List<Reminder>> = dao.getAllReminders()
    val activeReminders: Flow<List<Reminder>> = dao.getActiveReminders()
    val completedReminders: Flow<List<Reminder>> = dao.getCompletedReminders()

    suspend fun addReminder(reminder: Reminder): Long {
        val id = dao.insert(reminder)
        val created = reminder.copy(id = id)
        if (!created.isCompleted && created.targetTimeMillis > System.currentTimeMillis()) {
            AlarmScheduler.scheduleAlarm(context, created)
        }
        return id
    }

    suspend fun updateReminder(reminder: Reminder) {
        dao.update(reminder)
        if (!reminder.isCompleted && reminder.targetTimeMillis > System.currentTimeMillis()) {
            AlarmScheduler.scheduleAlarm(context, reminder)
        } else if (reminder.isCompleted) {
            AlarmScheduler.cancelAlarm(context, reminder.id)
            NotificationHelper.cancelNotification(context, reminder.id)
        }
    }

    suspend fun toggleCompleted(reminder: Reminder) {
        val newStatus = !reminder.isCompleted
        val updated = reminder.copy(isCompleted = newStatus)
        dao.setCompleted(reminder.id, newStatus)

        if (newStatus) {
            AlarmScheduler.cancelAlarm(context, reminder.id)
            NotificationHelper.cancelNotification(context, reminder.id)
        } else {
            if (reminder.targetTimeMillis > System.currentTimeMillis()) {
                AlarmScheduler.scheduleAlarm(context, updated)
            }
        }
    }

    suspend fun snoozeReminder(reminder: Reminder, snoozeMinutes: Int) {
        val newTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        val updated = reminder.copy(
            targetTimeMillis = newTime,
            isSnoozed = true,
            snoozeCount = reminder.snoozeCount + 1,
            isCompleted = false
        )
        dao.update(updated)
        NotificationHelper.cancelNotification(context, reminder.id)
        AlarmScheduler.scheduleAlarm(context, updated)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        AlarmScheduler.cancelAlarm(context, reminder.id)
        NotificationHelper.cancelNotification(context, reminder.id)
        dao.delete(reminder)

        // Clean up internal photo file if saved
        reminder.imagePath?.let { path ->
            try {
                val file = File(path)
                if (file.exists() && file.path.contains("reminders_photos")) {
                    file.delete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
