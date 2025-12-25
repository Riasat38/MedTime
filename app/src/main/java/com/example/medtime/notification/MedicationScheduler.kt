package com.example.medtime.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medtime.data.ParsedMedication
import java.util.Calendar

object MedicationScheduler {
    private const val TAG = "MedicationScheduler"
    
    fun scheduleMedications(
        context: Context,
        medications: List<ParsedMedication>,
        prescriptionId: String
    ) {
        Log.d(TAG, "Scheduling ${medications.size} medications for prescription: $prescriptionId")

        medications.forEachIndexed { medicationIndex, medication ->
            scheduleMedicationReminders(
                context = context,
                medication = medication,
                prescriptionId = prescriptionId,
                medicationIndex = medicationIndex
            )
        }
    }

    /**
     * Schedule reminders for a single medication at all its times
     */
    private fun scheduleMedicationReminders(
        context: Context,
        medication: ParsedMedication,
        prescriptionId: String,
        medicationIndex: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        medication.times.forEachIndexed { timeIndex, time ->
            val notificationId = generateNotificationId(prescriptionId, medicationIndex, timeIndex)

            val triggerTime = getNextTriggerTime(time)
            if (triggerTime != null) {
                scheduleAlarm(
                    context = context,
                    alarmManager = alarmManager,
                    medication = medication,
                    notificationId = notificationId,
                    triggerTimeMillis = triggerTime
                )
                Log.d(TAG, "Scheduled ${medication.name} at $time (id: $notificationId)")
            } else {
                Log.w(TAG, "Could not parse time: $time for ${medication.name}")
            }
        }
    }


    private fun scheduleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        medication: ParsedMedication,
        notificationId: Int,
        triggerTimeMillis: Long
    ) {
        val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
            putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, medication.name)
            putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, medication.dosage)
            putExtra(MedicationReminderReceiver.EXTRA_INSTRUCTIONS, medication.instructions)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // Use setRepeating for daily reminders
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                AlarmManager.INTERVAL_DAY, // Repeat daily
                pendingIntent
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "No permission to schedule exact alarm", e)
            // Fallback to inexact alarm
            alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    /**
     * Parse time string (HH:mm) and get next trigger time in milliseconds
     */
    private fun getNextTriggerTime(timeString: String): Long? {
        return try {
            val parts = timeString.split(":")
            if (parts.size != 2) return null

            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the time has already passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing time: $timeString", e)
            null
        }
    }

    /**
     * Generate unique notification ID based on prescription, medication, and time indices
     */
    private fun generateNotificationId(
        prescriptionId: String,
        medicationIndex: Int,
        timeIndex: Int
    ): Int {
        return (prescriptionId.hashCode() + medicationIndex * 100 + timeIndex)
    }

    /**
     * Cancel all reminders for a prescription
     */
    fun cancelMedicationReminders(
        context: Context,
        medications: List<ParsedMedication>,
        prescriptionId: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        medications.forEachIndexed { medicationIndex, medication ->
            medication.times.forEachIndexed { timeIndex, _ ->
                val notificationId = generateNotificationId(prescriptionId, medicationIndex, timeIndex)

                val intent = Intent(context, MedicationReminderReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    notificationId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()

                Log.d(TAG, "Cancelled reminder for ${medication.name} (id: $notificationId)")
            }
        }
    }
}

