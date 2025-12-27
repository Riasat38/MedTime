package com.example.medtime.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.medtime.data.UserSession
import com.example.medtime.repository.PrescriptionRepository
import com.example.medtime.utils.toParsedMedication
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
                // Get current user
                val user = UserSession.getUser()
                if (user == null) {
                    Log.e(TAG, "No user logged in, cannot reschedule reminders")
                    return@launch
                }

                // Get all prescriptions for user
                val result = PrescriptionRepository().getPrescriptionsForUser(user.uid)

                result.fold(
                    onSuccess = { allPrescriptions ->

                        val activePrescriptions = allPrescriptions.filter {
                            it.status == "active"
                        }

                        Log.d(TAG, "Found ${activePrescriptions.size} active prescriptions to reschedule")



                        activePrescriptions.forEach { prescription ->
                            MedicationScheduler.scheduleMedications(
                                context,
                                prescription.medications.mapNotNull { it.toParsedMedication()},
                                prescription.id
                            )
                        }

                        Log.d(TAG, "Successfully rescheduled ${activePrescriptions.size} prescriptions")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to fetch prescriptions for rescheduling", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error rescheduling reminders", e)
            }
        }
    }
}