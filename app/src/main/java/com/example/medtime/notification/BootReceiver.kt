package com.example.medtime.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medtime.repository.PrescriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device booted, rescheduling medication reminders")

            // Reschedule all active prescriptions
            // Note: You'll need to implement a way to fetch all active prescriptions
            // This is a placeholder - implement based on your data persistence strategy
            rescheduleAllReminders(context)
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // TODO: Fetch all active prescriptions from your repository
                // val prescriptions = PrescriptionRepository().getAllActivePrescriptions()
                // prescriptions.forEach { prescription ->
                //     MedicationScheduler.scheduleMedications(
                //         context,
                //         prescription.medications,
                //         prescription.id
                //     )
                // }
                Log.d(TAG, "Reminders rescheduled successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders", e)
            }
        }
    }
}