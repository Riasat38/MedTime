package com.example.medtime.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LogInScreen(
                onLoginSuccess = { email, name ->
                    // Navigate to home screen on successful login
                    navController.navigate("home/$email/$name") {
                        // Clear back stack so user can't go back to login
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpSuccess = { email, name ->
                    // Navigate to home screen on successful signup
                    navController.navigate("home/$email/$name") {
                        // Clear back stack so user can't go back to signup
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home/{email}/{name}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val name = backStackEntry.arguments?.getString("name") ?: ""
            HomeScreen(
                email = email,
                name = name,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
