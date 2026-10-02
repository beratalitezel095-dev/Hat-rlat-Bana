package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.Reminder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Hatırla", appName)
    }

    @Test
    fun `reminder default properties and snooze interval`() {
        val reminder = Reminder(
            title = "İlaç Al",
            notes = "Yemekten sonra 1 doz",
            targetTimeMillis = 1000000L,
            snoozeIntervalMinutes = 15
        )

        assertEquals("İlaç Al", reminder.title)
        assertEquals(15, reminder.snoozeIntervalMinutes)
        assertFalse(reminder.isCompleted)
        assertFalse(reminder.isSnoozed)
        assertEquals(0, reminder.snoozeCount)

        val snoozed = reminder.copy(
            isSnoozed = true,
            snoozeCount = reminder.snoozeCount + 1,
            targetTimeMillis = reminder.targetTimeMillis + (15 * 60 * 1000L)
        )

        assertTrue(snoozed.isSnoozed)
        assertEquals(1, snoozed.snoozeCount)
        assertEquals(1000000L + 900000L, snoozed.targetTimeMillis)
    }
}
