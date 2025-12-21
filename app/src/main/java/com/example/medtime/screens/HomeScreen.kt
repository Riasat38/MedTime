package com.example.medtime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.medtime.components.MedTimeTopAppBar
import com.example.medtime.ui.theme.MedTimeTheme
import com.example.medtime.data.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController? = null,
    modifier: Modifier = Modifier
) {
    val user = UserSession.getUser()
    val email = user?.email ?: ""
    val name = user?.name ?: ""
    val gender = user?.gender ?: ""
    val age = user?.age ?: ""
    Scaffold(
        topBar = {
            MedTimeTopAppBar()
        },
        bottomBar = {
            navController?.let {
                com.example.medtime.components.Navbar(
                    navController = it
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "User Information",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(text = "Name: $name")
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Email: $email")
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Gender: $gender")
            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Age: $age")
        }
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    MedTimeTheme {
        HomeScreen()
    }
}

