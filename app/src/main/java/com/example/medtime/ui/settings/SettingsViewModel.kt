package com.example.medtime.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application)

    val notificationPreference: StateFlow<NotificationPreference> = repository.notificationPreference

    fun setNotificationPreference(preference: NotificationPreference) {
        viewModelScope.launch {
            repository.setNotificationPreference(preference)
        }
    }
}
