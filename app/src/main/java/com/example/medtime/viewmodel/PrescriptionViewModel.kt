package com.example.medtime.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtime.ai.GeminiApiService
import com.example.medtime.data.MedicationResponse
import com.example.medtime.data.ParsedMedication
import com.example.medtime.data.UserSession
import com.example.medtime.notification.MedicationScheduler
import com.example.medtime.notification.NotificationHelper
import com.example.medtime.repository.PrescriptionRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.Uri
import android.graphics.BitmapFactory
private const val TAG = "PrescriptionViewModel"

class PrescriptionViewModel : ViewModel() {

    private val geminiApiService = GeminiApiService()
    private val prescriptionRepository = PrescriptionRepository()
    private val gson = Gson()

    var analysisState by mutableStateOf<AnalysisState>(AnalysisState.Idle)
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    var editableMedications by mutableStateOf<List<ParsedMedication>>(emptyList())
        private set

    var modelUsed by mutableStateOf<String?>(null)
        private set

    var saveState by mutableStateOf<SaveState>(SaveState.Idle)
        private set

    var title by mutableStateOf("")
        private set

    fun updateTitle(userTitle: String) {
        title = userTitle
    }

    fun setSelectedImage(uri: Uri) {
        selectedImageUri = uri
        analysisState = AnalysisState.Idle
        saveState = SaveState.Idle
    }

    fun analyzeImage(context: Context) {
        val uri = selectedImageUri ?: return

        analysisState = AnalysisState.Loading
        modelUsed = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting prescription analysis from URI")

                // Load a scaled-down bitmap from URI (memory efficient)
                val bitmap = loadScaledBitmap(context, uri)
                Log.d(TAG, "Loaded scaled bitmap: ${bitmap.width}x${bitmap.height}")

                val result = try {
                    withContext(Dispatchers.IO) {
                        geminiApiService.extractMedicationInfo(bitmap)
                    }
                } catch (e: OutOfMemoryError) {
                    Log.e(TAG, "Out of memory error during analysis", e)
                    bitmap.recycle()
                    analysisState = AnalysisState.Error("Out of memory. Please try with a smaller image.")
                    return@launch
                }

                // Clean up bitmap immediately after use
                bitmap.recycle()

                Log.d(TAG, "Received response from Gemini API using model: ${result.modelUsed}")
                modelUsed = result.modelUsed

                // Try to parse JSON response
                try {
                    // Clean the response to extract only JSON part
                    val jsonString = extractJsonFromResponse(result.response)
                    Log.d(TAG, "Extracted JSON: $jsonString")

                    val medicationResponse = gson.fromJson(jsonString, MedicationResponse::class.java)
                    editableMedications = medicationResponse.medications
                    analysisState = AnalysisState.Success(
                        medications = medicationResponse.medications,
                        modelUsed = result.modelUsed
                    )
                    Log.d(TAG, "Successfully parsed ${medicationResponse.medications.size} medications")
                } catch (e: Exception) {
                    Log.e(TAG, "JSON parsing failed", e)
                    // If JSON parsing fails, show raw response
                    analysisState = AnalysisState.Error(
                        "Failed to parse medication data: ${e.message}\n\nRaw response: ${result.response}"
                    )
                }
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory error", e)
                analysisState = AnalysisState.Error("Out of memory. Please try with a smaller image.")
            } catch (e: Exception) {
                Log.e(TAG, "Analysis failed", e)
                analysisState = AnalysisState.Error(
                    e.message ?: "Failed to analyze prescription. Please try again."
                )
            }
        }
    }

    private suspend fun loadScaledBitmap(context: Context, uri: Uri): Bitmap =
        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            // First pass: Get image dimensions without loading full image
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            // Calculate appropriate sample size
            val maxDimension = 2048
            options.inSampleSize = calculateInSampleSize(options, maxDimension)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Use less memory than ARGB_8888

            // Second pass: Load the scaled bitmap
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
                    ?: throw Exception("Failed to decode image")
            } ?: throw Exception("Failed to open image stream")
        }


    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        maxDimension: Int
    ): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > maxDimension || width > maxDimension) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize that keeps both dimensions >= maxDimension
            while (halfHeight / inSampleSize >= maxDimension &&
                halfWidth / inSampleSize >= maxDimension) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun extractJsonFromResponse(response: String): String {
        // Remove markdown code blocks if present
        var cleaned = response.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json").removeSuffix("```").trim()
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```").removeSuffix("```").trim()
        }
        return cleaned
    }

    fun updateMedication(index: Int, updatedMedication: ParsedMedication) {
        editableMedications = editableMedications.toMutableList().apply {
            set(index, updatedMedication)
        }
    }

    fun savePrescription(context: Context, notificationType: String) {
        val user = UserSession.getUser()
        if (user == null) {
            saveState = SaveState.Error("User not logged in")
            return
        }

        val model = modelUsed
        if (model == null) {
            saveState = SaveState.Error("No analysis to save")
            return
        }

        if (editableMedications.isEmpty()) {
            saveState = SaveState.Error("No medications to save")
            return
        }

        if (title.isBlank()) {
            saveState = SaveState.Error("Please enter a title to identify the prescription")
            return
        }

        saveState = SaveState.Saving

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    prescriptionRepository.savePrescription(
                        userId = user.uid,
                        medications = editableMedications,
                        modelUsed = model,
                        preccriptionTitle = title,
                        notificationType = notificationType
                    )
                }

                result.fold(
                    onSuccess = { prescriptionId ->
                        Log.d(TAG, "Prescription saved with ID: $prescriptionId")

                        // Create notification channel
                        NotificationHelper.createNotificationChannel(context)

                        // Schedule medication reminders
                        when (notificationType) {
                            "push" -> {
                                MedicationScheduler.scheduleMedications(
                                    context = context,
                                    medications = editableMedications,
                                    prescriptionId = prescriptionId
                                )
                                Log.d(TAG, "Scheduled push notifications for ${editableMedications.size} medications")
                            }
                            "alarm" -> {
                                // TODO: Implement alarm scheduling in next step
                                Log.d(TAG, "Alarm scheduling not yet implemented")
                            }
                        }
                        saveState = SaveState.Success(prescriptionId)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to save prescription", error)
                        saveState = SaveState.Error(error.message ?: "Failed to save prescription")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error saving prescription", e)
                saveState = SaveState.Error(e.message ?: "Failed to save prescription")
            }
        }
    }

    fun resetAnalysis() {
        analysisState = AnalysisState.Idle
        selectedImageUri = null
        editableMedications = emptyList()
        modelUsed = null
        saveState = SaveState.Idle
        title = ""
    }

    fun resetSaveState() {
        saveState = SaveState.Idle
    }

    fun clearError() {
        if (analysisState is AnalysisState.Error) {
            analysisState = AnalysisState.Idle
        }
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val prescriptionId: String) : SaveState()
    data class Error(val message: String) : SaveState()
}

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(
        val medications: List<ParsedMedication>,
        val modelUsed: String
    ) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}
