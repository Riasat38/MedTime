package com.example.medtime.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver to handle device boot and reschedule medication reminders
 * Note: This requires storing scheduled medications in SharedPreferences or database
 * For now, it just creates the notification channel
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, creating notification channel")

            // Create notification channel
            NotificationHelper.createNotificationChannel(context)

            // TODO: Reschedule alarms from stored data
            // This would require loading saved prescriptions from SharedPreferences or database
            // and calling MedicationScheduler.scheduleMedications() for each
        }
    }
}

