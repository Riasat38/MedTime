package com.example.medtime.data

import com.google.gson.annotations.SerializedName

data class ParsedMedication(
    val name: String = "",
    val dosage: String = "",
    val frequency: Int = 1,
    @SerializedName("frequency_type")
    val frequencyType: String = "daily",
    val times: List<String> = emptyList(),
    @SerializedName("duration_days")
    val durationDays: Int? = null,
    val instructions: String = "",
    val title: String = ""
)

data class MedicationResponse(
    val medications: List<ParsedMedication> = emptyList()
)

