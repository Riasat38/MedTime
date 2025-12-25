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

private const val TAG = "PrescriptionViewModel"

/**
 * ViewModel for PrescriptionScreen
 * Handles image analysis using Gemini AI and saving prescriptions
 */
class PrescriptionViewModel : ViewModel() {

    private val geminiApiService = GeminiApiService()
    private val prescriptionRepository = PrescriptionRepository()
    private val gson = Gson()

    var analysisState by mutableStateOf<AnalysisState>(AnalysisState.Idle)
        private set

    var selectedImage by mutableStateOf<Bitmap?>(null)
        private set

    var editableMedications by mutableStateOf<List<ParsedMedication>>(emptyList())
        private set

    var modelUsed by mutableStateOf<String?>(null)
        private set

    var saveState by mutableStateOf<SaveState>(SaveState.Idle)
        private set


    fun analyzePrescription(image: Bitmap) {
        // Store a scaled-down version to reduce memory
        selectedImage = image
        analysisState = AnalysisState.Loading
        modelUsed = null

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting prescription analysis. Bitmap size: ${image.width}x${image.height}")

                val result = try {
                    withContext(Dispatchers.IO) {
                        geminiApiService.extractMedicationInfo(image)
                    }
                } catch (e: OutOfMemoryError) {
                    Log.e(TAG, "Out of memory error during analysis", e)
                    analysisState = AnalysisState.Error("Out of memory. Please try with a smaller image.")
                    return@launch
                }

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

    fun savePrescription(context: Context) {
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

        saveState = SaveState.Saving

        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    prescriptionRepository.savePrescription(
                        userId = user.uid,
                        medications = editableMedications,
                        modelUsed = model
                    )
                }

                result.fold(
                    onSuccess = { prescriptionId ->
                        Log.d(TAG, "Prescription saved with ID: $prescriptionId")

                        // Create notification channel
                        NotificationHelper.createNotificationChannel(context)

                        // Schedule medication reminders
                        MedicationScheduler.scheduleMedications(
                            context = context,
                            medications = editableMedications,
                            prescriptionId = prescriptionId
                        )
                        Log.d(TAG, "Scheduled reminders for ${editableMedications.size} medications")

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
        selectedImage = null
        editableMedications = emptyList()
        modelUsed = null
        saveState = SaveState.Idle
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
