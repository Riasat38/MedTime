package com.example.medtime.ai

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.medtime.BuildConfig
import kotlin.math.min
private const val TAG = "GeminiApiService"

data class GeminiResult(
    val response: String,
    val modelUsed: String
)

class GeminiApiService {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val modelName = "gemini-2.5-flash"

    companion object {
        private const val TAG = "GeminiApiService"
        private const val MAX_IMAGE_DIMENSION = 2048 // Max dimension for API
    }
    suspend fun extractMedicationInfo(
        imageBitmap: Bitmap,
        context: String = "Extract medication information from this prescription"
    ): GeminiResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting extraction. Bitmap: ${imageBitmap.width}x${imageBitmap.height}, config: ${imageBitmap.config}")

        // Ensure bitmap is in correct format and size
        val safeBitmap = prepareSafeBitmap(imageBitmap)

        val prompt = buildPrompt(context)

        try {
            Log.d(TAG, "Using model: $modelName")

            val generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.0f
                    topK = 1
                    topP = 1f
                }
            )

            val content = content {
                text(prompt)
                image(safeBitmap)
            }

            val response = generativeModel.generateContent(content)
            Log.d(TAG, "Successfully received response")

            val text = response.text
            if (text.isNullOrBlank()) {
                throw Exception("Empty response from AI")
            }
            if (safeBitmap != imageBitmap) {
                safeBitmap.recycle()
            }
            GeminiResult(response = text, modelUsed = modelName)

        } catch (e: Exception) {
            Log.e(TAG, "API call failed: ${e.message}", e)

            if (safeBitmap != imageBitmap) {
                safeBitmap.recycle()
            }

            throw Exception("Failed to analyze prescription: ${e.message}")
        }
    }

    private fun prepareSafeBitmap(bitmap: Bitmap): Bitmap {
        var workingBitmap = bitmap

        // Step 1: Scale down if too large
        if (bitmap.width > MAX_IMAGE_DIMENSION || bitmap.height > MAX_IMAGE_DIMENSION) {
            val ratio = minOf(
                MAX_IMAGE_DIMENSION.toFloat() / bitmap.width,
                MAX_IMAGE_DIMENSION.toFloat() / bitmap.height
            )
            val newWidth = (bitmap.width * ratio).toInt()
            val newHeight = (bitmap.height * ratio).toInt()

            Log.d(TAG, "Scaling bitmap from ${bitmap.width}x${bitmap.height} to ${newWidth}x${newHeight}")

            workingBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

            // If we created a new bitmap, we can recycle the original
            if (workingBitmap != bitmap) {
                bitmap.recycle()
            }
        }

        // Step 2: Convert to RGB_565 if it's ARGB_8888 (saves 50% memory)
        if (workingBitmap.config == Bitmap.Config.ARGB_8888) {
            Log.d(TAG, "Converting bitmap from ARGB_8888 to RGB_565")

            val rgb565Bitmap = workingBitmap.copy(Bitmap.Config.RGB_565, false)

            // Recycle the ARGB bitmap if we successfully created RGB version
            if (rgb565Bitmap != null) {
                if (workingBitmap != bitmap) {
                    workingBitmap.recycle()
                }
                workingBitmap = rgb565Bitmap
            }
        }

        Log.d(TAG, "Final bitmap: ${workingBitmap.width}x${workingBitmap.height}, config: ${workingBitmap.config}")

        return workingBitmap
    }

    private fun buildPrompt(context: String): String {
        return """
                $context
You are a medical prescription analyzer. Extract ALL medication information from this prescription image and return it in the specified JSON format.

CRITICAL INSTRUCTIONS:

1. LANGUAGE HANDLING:
   - Prescriptions may contain Bengali, English, or mixed text
   - Bengali numbers: ০=0, ১=1, ২=2, ৩=3, ৪=4, ৫=5, ৬=6, ৭=7, ৮=8, ৯=9
   - Bengali duration words: দিন=days, সপ্তাহ=weeks, মাস=months, বছর=years
   - Convert all numbers to English numerals in output

2. FREQUENCY EXTRACTION: Always convert to numeric value representing doses per day:
   - "once daily" / "OD" / "১বার" → 1
   - "twice daily" / "BD" / "BID" / "২বার" → 2
   - "thrice daily" / "TDS" / "TID" / "৩বার" → 3
   - "four times daily" / "QID" / "৪বার" → 4
   - "once weekly" / "once a week" / "সপ্তাহে একবার" → frequency_type: "weekly", frequency: 1
   - "twice weekly" / "সপ্তাহে দুইবার" → frequency_type: "weekly", frequency: 2
   - "once monthly" / "মাসে একবার" → frequency_type: "monthly", frequency: 1
   - "twice monthly" / "মাসে দুইবার" → frequency_type: "monthly", frequency: 2

3. DURATION EXTRACTION (CRITICAL):
   - Look for duration NEXT TO or AFTER each medicine name
   - Patterns to detect:
     * "X দিন" / "X days" → X days
     * "X সপ্তাহ" / "X weeks" → X*7 days
     * "X মাস" / "X months" → X*30 days
     * "X বছর" / "X years" → X*365 days
     * "for X days/weeks/months"
     * Numbers followed by "দিন/days" or "সপ্তাহ/weeks"
   - Convert Bengali numerals to English
   - Examples: "১৫ দিন" → 15, "৭ দিন" → 7, "২ সপ্তাহ" → 14, "১ মাস" → 30
   - If duration appears ONCE at the bottom for ALL medicines, apply to ALL
   - If no duration found, use null

4. TIMES GENERATION: Generate appropriate time slots based on frequency:
   - Once daily → ["09:00"]
   - Twice daily → ["09:00", "21:00"]
   - Thrice daily → ["09:00", "15:00", "21:00"]
   - Four times daily → ["08:00", "14:00", "20:00", "22:00"]
   - Weekly medications → ["09:00"]
   - Monthly medications → ["09:00"]
   - Specific times: সকাল/morning=08:00, দুপুর/afternoon=14:00, সন্ধ্যা/evening=18:00, রাত/night=21:00
   - Meal-based: খাবার আগে/before meals → breakfast=08:00, lunch=13:00, dinner=20:00
   - খাবার পরে/after meals → 09:00, 14:00, 21:00

5. COMMON NOTATIONS:
   - "1-0-1" / "1+0+1" / "১-০-১" → frequency: 2, times: ["09:00", "21:00"]
   - "1-1-1" / "১-১-১" → frequency: 3, times: ["09:00", "15:00", "21:00"]
   - "0-0-1" / "০-০-১" → frequency: 1, times: ["21:00"]
   - "1-0-0" / "১-০-০" → frequency: 1, times: ["09:00"]
   - "0-1-0" / "০-১-০" → frequency: 1, times: ["15:00"]

6. SPECIAL INSTRUCTIONS TO CAPTURE:
   - খাবার আগে / before food / before meals
   - খাবার পরে / after food / after meals
   - ঘুমানোর আগে / before sleep / at bedtime
   - খালি পেটে / empty stomach
   - পানির সাথে / with water

7. DEFAULTS (if unclear or missing):
   - frequency: 1
   - frequency_type: "daily"
   - times: ["09:00"]
   - duration_days: null

OUTPUT FORMAT (strict JSON only, no additional text):
{
    "medications": [
        {
            "name": "Medicine name exactly as written",
            "dosage": "e.g., 500mg, 10ml, 1 tablet",
            "frequency": 2,
            "frequency_type": "daily",
            "times": ["09:00", "21:00"],
            "duration_days": 15,
            "instructions": "After food, before sleep, etc."
        }
    ]
}

IMPORTANT RULES:
- frequency_type must be one of: "daily", "weekly", "monthly"
- duration_days must be a NUMBER (convert Bengali to English) or null
- Extract duration for EACH medicine individually
- Convert ALL Bengali text/numbers in output to English
- Return ONLY valid JSON, no preamble, no explanation, no markdown

Analyze the prescription carefully and extract each medicine with its specific duration.
                """.trimIndent()
    }
}