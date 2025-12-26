package com.example.medtime.ui.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _notificationPreference = MutableStateFlow(getNotificationPreference())
    val notificationPreference: StateFlow<NotificationPreference> = _notificationPreference

    fun getNotificationPreference(): NotificationPreference {
        val preferenceString = sharedPreferences.getString("notification_preference", NotificationPreference.LOCAL.name)
        return NotificationPreference.valueOf(preferenceString ?: NotificationPreference.LOCAL.name)
    }

    fun setNotificationPreference(preference: NotificationPreference) {
        sharedPreferences.edit().putString("notification_preference", preference.name).apply()
        _notificationPreference.value = preference
    }
}
