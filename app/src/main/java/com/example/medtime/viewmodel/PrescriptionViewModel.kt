package com.example.medtime.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtime.ai.GeminiApiService
import kotlinx.coroutines.launch

/**
 * ViewModel for PrescriptionScreen
 * Handles image analysis using Gemini AI
 */
class PrescriptionViewModel : ViewModel() {

    private val geminiApiService = GeminiApiService()

    var analysisState by mutableStateOf<AnalysisState>(AnalysisState.Idle)
        private set

    var selectedImage by mutableStateOf<Bitmap?>(null)
        private set


    fun analyzePrescription(image: Bitmap) {
        selectedImage = image
        analysisState = AnalysisState.Loading

        viewModelScope.launch {
            try {
                val response = geminiApiService.extractMedicationInfo(image)
                analysisState = AnalysisState.Success(response)
            } catch (e: Exception) {
                analysisState = AnalysisState.Error(
                    e.message ?: "Failed to analyze prescription. Please try again."
                )
            }
        }
    }

    fun resetAnalysis() {
        analysisState = AnalysisState.Idle
        selectedImage = null
    }


    fun clearError() {
        if (analysisState is AnalysisState.Error) {
            analysisState = AnalysisState.Idle
        }
    }
}

sealed class AnalysisState {
    object Idle : AnalysisState()
    object Loading : AnalysisState()
    data class Success(val result: String) : AnalysisState()
    data class Error(val message: String) : AnalysisState()
}
