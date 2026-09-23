package com.kingsley.wong.medtrack.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.kingsley.wong.medtrack.R
import com.kingsley.wong.medtrack.data.entity.Medication
import java.util.Calendar

object NotificationHelper {

    private const val CHANNEL_ID = "medication_reminders"
    private const val CHANNEL_NAME = "Medication Reminders"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medications"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showMedicationReminder(context: Context, medicationName: String, time: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Medication Reminder")
            .setContentText("Time to take $medicationName at $time")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(medicationName.hashCode(), notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleMedicationReminders(context: Context, medications: List<Medication>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if we can schedule exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Fall back to inexact alarm if permission not granted
                scheduleInexactReminders(context, medications)
                return
            }
        }

        medications.forEach { med ->
            // Parse the scheduled time (format: "HH:mm")
            val timeParts = med.scheduledTime.split(":")
            if (timeParts.size != 2) return@forEach

            val hour = timeParts[0].toIntOrNull() ?: return@forEach
            val minute = timeParts[1].toIntOrNull() ?: return@forEach

            // Set calendar to today at the medication's scheduled time
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If the time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            // Create intent for the receiver
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("medication_name", med.medicationName)
                putExtra("medication_time", med.scheduledTime)
                putExtra("medication_id", med.id)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                med.id, // unique request code per medication
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule repeating daily alarm
            // Schedule exact alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelAllReminders(context: Context, medications: List<Medication>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        medications.forEach { med ->
            val intent = Intent(context, MedicationReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                med.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
    private fun scheduleInexactReminders(context: Context, medications: List<Medication>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        medications.forEach { med ->
            val timeParts = med.scheduledTime.split(":")
            if (timeParts.size != 2) return@forEach

            val hour = timeParts[0].toIntOrNull() ?: return@forEach
            val minute = timeParts[1].toIntOrNull() ?: return@forEach

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("medication_name", med.medicationName)
                putExtra("medication_time", med.scheduledTime)
                putExtra("medication_id", med.id)
            }

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, med.id, intent, flags)

            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}