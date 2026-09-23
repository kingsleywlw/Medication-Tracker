package com.kingsley.wong.medtrack.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kingsley.wong.medtrack.data.AppDatabase
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.util.Calendar

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicationName = intent.getStringExtra("medication_name") ?: "your medication"
        val time = intent.getStringExtra("medication_time") ?: ""
        val medId = intent.getIntExtra("medication_id", -1)

        // Check if medication is already taken today
        val shouldNotify = if (medId != -1) {
            runBlocking {
                try {
                    val dao = AppDatabase.getDatabase(context).medicationDao()
                    val med = dao.getMedicationById(medId)
                    val today = LocalDate.now().toString()
                    med == null || !med.isTaken || med.takenDate != today
                } catch (e: Exception) {
                    true
                }
            }
        } else {
            true
        }

        if (shouldNotify) {
            NotificationHelper.showMedicationReminder(context, medicationName, time)
        }

        // Reschedule for tomorrow (since we use exact alarm, not repeating)
        rescheduleForTomorrow(context, intent, medId, time)
    }

    @android.annotation.SuppressLint("ScheduleExactAlarm")
    private fun rescheduleForTomorrow(context: Context, originalIntent: Intent, medId: Int, time: String) {
        val timeParts = time.split(":")
        if (timeParts.size != 2) return

        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val newIntent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra("medication_name", originalIntent.getStringExtra("medication_name"))
            putExtra("medication_time", time)
            putExtra("medication_id", medId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getBroadcast(context, medId, newIntent, flags)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}