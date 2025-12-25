package com.example.medtime.repository

import android.util.Log
import com.example.medtime.data.ParsedMedication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

private const val TAG = "PrescriptionRepository"

data class SavedPrescription(
    val id: String = "",
    val userId: String = "",
    val medications: List<Map<String, Any?>> = emptyList(),
    val modelUsed: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val status: String = "active"
)

class PrescriptionRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val prescriptionsCollection = firestore.collection("prescriptions")

    suspend fun savePrescription(
        userId: String,
        medications: List<ParsedMedication>,
        modelUsed: String
    ): Result<String> {
        return try {
            val prescriptionId = UUID.randomUUID().toString()

            // Convert medications to a list of maps for Firestore
            val medicationMaps = medications.map { medication ->
                mapOf(
                    "name" to medication.name,
                    "dosage" to medication.dosage,
                    "frequency" to medication.frequency,
                    "frequencyType" to medication.frequencyType,
                    "times" to medication.times,
                    "durationDays" to medication.durationDays,
                    "instructions" to medication.instructions
                )
            }

            val prescriptionData = hashMapOf(
                "id" to prescriptionId,
                "userId" to userId,
                "medications" to medicationMaps,
                "modelUsed" to modelUsed,
                "createdAt" to Timestamp.now(),
                "status" to "active"
            )

            prescriptionsCollection.document(prescriptionId).set(prescriptionData).await()

            Log.d(TAG, "Prescription saved successfully with ID: $prescriptionId")
            Result.success(prescriptionId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save prescription", e)
            Result.failure(e)
        }
    }

    suspend fun getPrescriptionsForUser(userId: String): Result<List<SavedPrescription>> {
        return try {
            val snapshot = prescriptionsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val prescriptions = snapshot.documents.mapNotNull { doc ->
                try {
                    SavedPrescription(
                        id = doc.getString("id") ?: "",
                        userId = doc.getString("userId") ?: "",
                        medications = (doc.get("medications") as? List<Map<String, Any?>>) ?: emptyList(),
                        modelUsed = doc.getString("modelUsed") ?: "",
                        createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                        status = doc.getString("status") ?: "active"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing prescription document", e)
                    null
                }
            }

            Result.success(prescriptions)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get prescriptions", e)
            Result.failure(e)
        }
    }

    suspend fun deletePrescription(prescriptionId: String): Result<Unit> {
        return try {
            prescriptionsCollection.document(prescriptionId).delete().await()
            Log.d(TAG, "Prescription deleted successfully: $prescriptionId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete prescription", e)
            Result.failure(e)
        }
    }
}

