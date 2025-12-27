package com.example.medtime.utils

import com.example.medtime.data.ParsedMedication

fun Map<String, Any?>.toParsedMedication(): ParsedMedication? {
    return try {
        ParsedMedication(
            name = this["name"] as? String ?: "",
            dosage = this["dosage"] as? String ?: "",
            frequency = when (val freq = this["frequency"]) {
                is Long -> freq.toInt()
                is Int -> freq
                is Double -> freq.toInt()
                is String -> freq.toIntOrNull() ?: 1
                else -> 1
            },
            frequencyType = this["frequencyType"] as? String ?: "daily",
            times = (this["times"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            durationDays = when (val duration = this["durationDays"]) {
                is Long -> duration.toInt()
                is Int -> duration
                is Double -> duration.toInt()
                is String -> duration.toIntOrNull()
                else -> null
            },
            instructions = this["instructions"] as? String ?: "",
            title = this["title"] as? String ?: ""
        )
    } catch (e: Exception) {
        null
    }
}