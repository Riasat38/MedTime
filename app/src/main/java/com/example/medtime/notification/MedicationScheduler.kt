package com.example.medtime.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.medtime.data.ParsedMedication
import java.util.*
import kotlin.math.absoluteValue


object MedicationScheduler {

    private const val TAG = "MedicationScheduler"


    fun scheduleMedications(
        context: Context,
        medications: List<ParsedMedication>,
        prescriptionId: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var totalScheduled = 0

        medications.forEachIndexed { medIndex, medication ->
            // Parse duration int -> string
            val durationDays = parseDuration(medication.durationDays.toString())

            if (durationDays <= 0) {
                Log.w(TAG, "Invalid duration for ${medication.name}: ${medication.durationDays}")
                return@forEachIndexed
            }

            medication.times.forEachIndexed { timeIndex, time ->
                // Schedule notification for each day in the duration
                for (day in 0 until durationDays) {
                    scheduleMedicationReminder(
                        context = context,
                        alarmManager = alarmManager,
                        medication = medication,
                        time = time,
                        dayOffset = day,
                        medicationIndex = medIndex,
                        timeIndex = timeIndex,
                        prescriptionId = prescriptionId
                    )
                    totalScheduled++
                }
            }

            Log.d(TAG, "Scheduled ${medication.times.size} times × $durationDays days = ${medication.times.size * durationDays} notifications for ${medication.name}")
        }

        Log.d(TAG, "Total notifications scheduled: $totalScheduled")
    }

    private fun parseDuration(duration: String): Int {
        return try {
            // Remove non-numeric characters and parse
            val cleaned = duration.replace(Regex("[^0-9]"), "")
            cleaned.toIntOrNull() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing duration: $duration", e)
            0
        }
    }


    private fun scheduleMedicationReminder(
        context: Context,
        alarmManager: AlarmManager,
        medication: ParsedMedication,
        time: String,
        dayOffset: Int,
        medicationIndex: Int,
        timeIndex: Int,
        prescriptionId: String
    ) {
        try {
            // Parse time (format: "HH:mm" or "H:mm")
            val timeParts = time.trim().replace(" ", "").split(":")
            if (timeParts.size != 2) {
                Log.e(TAG, "Invalid time format: $time")
                return
            }

            val hour = timeParts[0].toIntOrNull()
            val minute = timeParts[1].toIntOrNull()

            if (hour == null || minute == null || hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                Log.e(TAG, "Invalid time values: $time")
                return
            }

            // Create calendar for the reminder time
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // Add day offset
                add(Calendar.DAY_OF_YEAR, dayOffset)

                // If this specific time has already passed, skip it
                if (timeInMillis <= System.currentTimeMillis()) {
                    Log.d(TAG, "Skipping past time: ${medication.name} on day $dayOffset at $time")
                    return
                }
            }

            // Create unique request code for this specific notification
            val requestCode = generateRequestCode(prescriptionId, medicationIndex, timeIndex, dayOffset)

            // Create intent for the broadcast receiver
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra(MedicationReminderReceiver.EXTRA_MEDICATION_NAME, medication.name)
                putExtra(MedicationReminderReceiver.EXTRA_DOSAGE, medication.dosage)
                putExtra(MedicationReminderReceiver.EXTRA_INSTRUCTIONS, medication.instructions)
                putExtra(MedicationReminderReceiver.EXTRA_NOTIFICATION_ID, requestCode)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the exact alarm
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "✓ Scheduled ${medication.name} for Day ${dayOffset + 1} at ${calendar.time}")

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling medication: ${medication.name}", e)
        }
    }

    private fun generateRequestCode(
        prescriptionId: String,
        medicationIndex: Int,
        timeIndex: Int,
        dayOffset: Int
    ): Int {
        // Create a unique code using prescription ID hash + indices + day
        val hashCode = prescriptionId.hashCode()
        return (hashCode + medicationIndex * 100000 + timeIndex * 10000 + dayOffset).absoluteValue % Int.MAX_VALUE
    }

    fun cancelMedicationReminders(
        context: Context,
        prescriptionId: String,
        medications: List<ParsedMedication>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var totalCancelled = 0

        medications.forEachIndexed { medIndex, medication ->
            val durationDays = parseDuration(medication.durationDays.toString())

            medication.times.forEachIndexed { timeIndex, _ ->
                // Cancel notification for each day
                for (day in 0 until durationDays) {
                    val requestCode = generateRequestCode(prescriptionId, medIndex, timeIndex, day)

                    val intent = Intent(context, MedicationReminderReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    alarmManager.cancel(pendingIntent)
                    totalCancelled++
                }
            }
        }

        Log.d(TAG, "Cancelled $totalCancelled notifications for prescription: $prescriptionId")
    }

    fun getTotalNotificationCount(medications: List<ParsedMedication>): Int {
        var total = 0
        medications.forEach { medication ->
            val durationDays = parseDuration(medication.durationDays.toString())
            total += medication.times.size * durationDays
        }
        return total
    }


    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }


    fun getScheduledTimesPreview(medications: List<ParsedMedication>): List<String> {
        val preview = mutableListOf<String>()

        medications.forEach { medication ->
            val durationDays = parseDuration(medication.durationDays.toString())
            medication.times.forEach { time ->
                preview.add("${medication.name} at $time for $durationDays days")
            }
        }

        return preview
    }
}