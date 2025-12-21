package com.example.medtime

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.medtime.data.UserSession
import com.example.medtime.screens.AppNavigation
import com.example.medtime.ui.theme.MedTimeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize session management
        UserSession.initialize(this)

        enableEdgeToEdge()
        setContent {
            MedTimeTheme {
                MedTimeApp()
            }
        }
    }
}

@Composable
fun MedTimeApp() {
    AppNavigation()
}
