package com.example.medtime.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtime.data.ParsedMedication
import com.example.medtime.data.UserSession
import com.example.medtime.repository.PrescriptionRepository
import com.example.medtime.repository.SavedPrescription
import kotlinx.coroutines.launch

class PrescriptionListViewModel : ViewModel() {

    private val repository = PrescriptionRepository()

    var prescriptionsState by mutableStateOf<PrescriptionListState>(PrescriptionListState.Loading)
        private set

    fun loadPrescriptions() {
        val user = UserSession.getUser()
        if (user == null) {
            prescriptionsState = PrescriptionListState.Error("User not logged in")
            return
        }

        prescriptionsState = PrescriptionListState.Loading

        viewModelScope.launch {
            repository.getPrescriptionsForUser(user.uid).fold(
                onSuccess = { prescriptions ->
                    prescriptionsState = PrescriptionListState.Success(prescriptions)
                },
                onFailure = { error ->
                    prescriptionsState = PrescriptionListState.Error(
                        error.message ?: "Failed to load prescriptions"
                    )
                }
            )
        }
    }

    fun deletePrescription(prescriptionId: String) {
        viewModelScope.launch {
            repository.deletePrescription(prescriptionId).fold(
                onSuccess = {
                    // Reload prescriptions after deletion
                    loadPrescriptions()
                },
                onFailure = { error ->
                    prescriptionsState = PrescriptionListState.Error(
                        error.message ?: "Failed to delete prescription"
                    )
                }
            )
        }
    }
}

sealed class PrescriptionListState {
    object Loading : PrescriptionListState()
    data class Success(val prescriptions: List<SavedPrescription>) : PrescriptionListState()
    data class Error(val message: String) : PrescriptionListState()
}