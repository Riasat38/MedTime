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
                onLoginClick = { email, _ ->
                    // TODO: Implement login logic here
                    // For now, navigate to home screen
                    navController.navigate("home/$email")
                },
                onSignUpClick = { navController.navigate("signup") }
            )
        }
        composable("signup") {
            SignUpScreen(
                onBackClick = { navController.popBackStack() },
                onSignUpClick = { name, _, _, _, _ ->

                    // For now, navigate back to login after successful signup
                    navController.navigate("home/$name")
                }
            )
        }
        composable("home/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            HomeScreen(email = email, modifier = Modifier.fillMaxSize())
        }
    }
}
