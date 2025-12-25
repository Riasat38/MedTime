package com.example.medtime.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "MedicationReminder"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_INSTRUCTIONS = "instructions"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received medication reminder broadcast")

        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val instructions = intent.getStringExtra(EXTRA_INSTRUCTIONS) ?: ""

        Log.d(TAG, "Showing notification for: $medicationName at time, id: $notificationId")

        // Create notification channel if needed
        NotificationHelper.createNotificationChannel(context)

        // Show the notification
        NotificationHelper.showMedicationNotification(
            context = context,
            notificationId = notificationId,
            medicationName = medicationName,
            dosage = dosage,
            instructions = instructions
        )
    }
}

