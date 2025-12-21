package com.example.medtime.ai

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.medtime.BuildConfig


class GeminiApiService() {
    val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun extractMedicationInfo(
        imageBitmap: Bitmap,
        context: String = "Extract medication information from this prescription"
    ): String = withContext(Dispatchers.IO) {
        try {
            val generativeModel = GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = apiKey
            )
            val prompt = """
                $context

                Extract medication information in this JSON format:
                {
                    "medications": [
                        {
                            "name": "Medicine name",
                            "dosage": "e.g., 500mg",
                            "frequency": "e.g., Twice daily",
                            "times": ["08:00", "20:00"],
                            "duration_days": 7,
                            "instructions": "Additional instructions"
                        }
                    ]
                }

                Only return valid JSON.
            """.trimIndent()

            // Call Gemini with image
            val content = content {
                text(prompt)
                image(imageBitmap)
            }

            val response = generativeModel.generateContent(content)

            response.text ?: throw Exception("No response from AI")
        } catch (e: Exception) {
            throw Exception("Failed to extract information: ${e.message}")
        }
    }
}