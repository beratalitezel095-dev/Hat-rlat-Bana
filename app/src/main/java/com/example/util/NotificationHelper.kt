package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.Reminder
import com.example.receiver.AlarmReceiver

object NotificationHelper {

    const val CHANNEL_ID = "channel_reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_reminders_name)
            val descriptionText = context.getString(R.string.channel_reminders_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showReminderNotification(context: Context, reminder: Reminder) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Content intent when user taps the notification
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("reminder_id", reminder.id)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            reminder.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze (Ertele)
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_SNOOZE_REMINDER
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(AlarmReceiver.EXTRA_SNOOZE_MINUTES, reminder.snoozeIntervalMinutes)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 1).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Done (Tamamlandı)
        val doneIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_COMPLETE_REMINDER
            putExtra(AlarmReceiver.EXTRA_REMINDER_ID, reminder.id)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminder.id * 10 + 2).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeLabel = "${reminder.snoozeIntervalMinutes} dk Ertele"
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ " + reminder.title)
            .setContentText(
                if (reminder.notes.isNotBlank()) {
                    reminder.notes
                } else {
                    "[${reminder.category}] Zamanı geldi!"
                }
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(0, snoozeLabel, snoozePendingIntent)
            .addAction(0, "✓ Tamamlandı", donePendingIntent)

        // If there's an image, attach BigPictureStyle
        reminder.imagePath?.let { path ->
            val bitmap = ImageStorageHelper.loadBitmapFromPath(path)
            if (bitmap != null) {
                val bigPicStyle = NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .setBigContentTitle("⏰ " + reminder.title)
                    .setSummaryText(if (reminder.notes.isNotBlank()) reminder.notes else reminder.category)
                builder.setStyle(bigPicStyle)
                builder.setLargeIcon(bitmap)
            }
        }

        notificationManager.notify(reminder.id.toInt(), builder.build())
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(reminderId.toInt())
    }
}
