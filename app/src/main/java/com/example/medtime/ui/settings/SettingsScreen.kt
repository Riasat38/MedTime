package com.example.medtime.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val notificationPreference by viewModel.notificationPreference.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Row {
            Text("Enable Push Notifications")
            Switch(
                checked = notificationPreference == NotificationPreference.PUSH,
                onCheckedChange = { isChecked ->
                    val newPreference = if (isChecked) {
                        NotificationPreference.PUSH
                    } else {
                        NotificationPreference.LOCAL
                    }
                    viewModel.setNotificationPreference(newPreference)
                }
            )
        }
    }
}
