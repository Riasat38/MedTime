package com.example.medtime.data
import com.google.firebase.Timestamp


data class Medication(
    val id: String = "",
    val userId: String = "",
    val name: String,
    val dosage: String,
    val frequency: String,
    val durationDays: Int,
    val times: List<String>,
    val startDate: Timestamp = Timestamp.now(),
    val endDate: Timestamp,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Timestamp = Timestamp.now()
)


data class Prescription(
    val id: String = "",
    val title: String = "",
    val medications: List<ParsedMedication> = emptyList(),
    val modelUsed: String = "",
    val notificationType:String ="",
    val createdAt: Long = 0L
)