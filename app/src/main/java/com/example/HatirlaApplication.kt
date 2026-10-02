package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.data.ReminderRepository
import com.example.util.NotificationHelper

class HatirlaApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { ReminderRepository(database.reminderDao(), this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
