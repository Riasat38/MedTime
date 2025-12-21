package com.example.medtime.data
import com.google.firebase.Timestamp

// Medication.kt
data class Medication(
    val id: String = "",
    val userId: String = "",
    val name: String,
    val dosage: String,
    val frequency: String,
    val durationDays: Int,
    val times: List<String>, // e.g., ["08:00", "14:00", "20:00"]
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp,
    val notes: String = "",
    val isActive: Boolean = true
)

// Prescription.kt
data class Prescription(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String,
    val extractedText: String? = null,
    val medications: List<Medication> = emptyList(),
    val uploadDate: Timestamp = Timestamp.now(),
    val status: String = "pending" // pending, processed, error
)