package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.model.Reminder
import com.example.util.AlarmScheduler
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SHOW_REMINDER = "com.example.ACTION_SHOW_REMINDER"
        const val ACTION_SNOOZE_REMINDER = "com.example.ACTION_SNOOZE_REMINDER"
        const val ACTION_COMPLETE_REMINDER = "com.example.ACTION_COMPLETE_REMINDER"

        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_NOTES = "extra_notes"
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId == -1L) return

        val database = AppDatabase.getInstance(context)
        val dao = database.reminderDao()
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_SHOW_REMINDER -> {
                        val reminder = dao.getReminderByIdDirect(reminderId)
                        if (reminder != null && !reminder.isCompleted) {
                            NotificationHelper.showReminderNotification(context, reminder)
                        } else if (reminder == null) {
                            // Fallback using extras
                            val title = intent.getStringExtra(EXTRA_TITLE) ?: "Hatırlatıcı"
                            val notes = intent.getStringExtra(EXTRA_NOTES) ?: ""
                            val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
                            val snoozeMin = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)
                            val fallback = Reminder(
                                id = reminderId,
                                title = title,
                                notes = notes,
                                imagePath = imagePath,
                                targetTimeMillis = System.currentTimeMillis(),
                                snoozeIntervalMinutes = snoozeMin
                            )
                            NotificationHelper.showReminderNotification(context, fallback)
                        }
                    }

                    ACTION_SNOOZE_REMINDER -> {
                        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10)
                        val newTargetTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)

                        dao.snoozeReminder(reminderId, newTargetTime)
                        val updated = dao.getReminderByIdDirect(reminderId)
                        if (updated != null) {
                            AlarmScheduler.scheduleAlarm(context, updated)
                        }

                        // Dismiss existing notification
                        NotificationHelper.cancelNotification(context, reminderId)

                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                "Hatırlatıcı $snoozeMinutes dakika ertelendi ⏰",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    ACTION_COMPLETE_REMINDER -> {
                        dao.setCompleted(reminderId, true)
                        NotificationHelper.cancelNotification(context, reminderId)
                        AlarmScheduler.cancelAlarm(context, reminderId)

                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                "Harika! Hatırlatıcı tamamlandı ✓",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
