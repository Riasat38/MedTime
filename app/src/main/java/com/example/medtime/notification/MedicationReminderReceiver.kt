package com.example.medtime.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MedicationReminderReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "MedicationReceiver"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_DOSAGE = "dosage"
        const val EXTRA_INSTRUCTIONS = "instructions"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Medication reminder received")

        // Extract medication details from intent
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medication"
        val dosage = intent.getStringExtra(EXTRA_DOSAGE) ?: ""
        val instructions = intent.getStringExtra(EXTRA_INSTRUCTIONS) ?: ""
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0)

        Log.d(TAG, "Showing notification for: $medicationName at $dosage")

        // Show the notification
        NotificationHelper.showMedicationNotification(
            context = context,
            medicationName = medicationName,
            dosage = dosage,
            instructions = instructions,
            notificationId = notificationId
        )
    }
}