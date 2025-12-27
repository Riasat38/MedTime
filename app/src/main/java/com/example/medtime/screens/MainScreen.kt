package com.example.medtime.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medtime.components.Navbar
import com.example.medtime.data.UserSession
import com.example.medtime.viewmodel.PrescriptionViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // APP START
    val startDestination = if (UserSession.isLoggedIn()) "home" else "login"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LogInScreen(
                onLoginSuccess = { user ->
                    UserSession.setUser(user)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpSuccess = { user ->
                    UserSession.setUser(user)
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                modifier = Modifier.fillMaxSize()
            )
        }
        composable("prescription") {
            val viewModel: PrescriptionViewModel = viewModel(
                viewModelStoreOwner = it
            )
            PrescriptionScreen(
                navController = navController,
                viewModel = viewModel
            )
        }
        composable("records") {
            RecordListScreen(navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionPlaceholder(
    navController: androidx.navigation.NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Prescription") }
            )
        },
        bottomBar = {
            Navbar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Prescription Screen (Coming Soon)",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsPlaceholder(
    navController: androidx.navigation.NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Records") }
            )
        },
        bottomBar = {
            Navbar(navController = navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Records Screen (Coming Soon)",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
