package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val imagePath: String? = null,
    val targetTimeMillis: Long,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val isSnoozed: Boolean = false,
    val snoozeCount: Int = 0,
    val snoozeIntervalMinutes: Int = 10, // Regular interval (dakika cinsinden düzenli erteleme)
    val category: String = "Genel",
    val priority: String = "NORMAL"
)
