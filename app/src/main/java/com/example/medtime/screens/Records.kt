package com.example.medtime.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medtime.components.ExpandablePrescriptionCard
import com.example.medtime.components.MedTimeTopAppBar
import com.example.medtime.data.UserSession
import com.example.medtime.viewmodel.PrescriptionListState
import com.example.medtime.viewmodel.PrescriptionListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordListScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: PrescriptionListViewModel = viewModel()
) {
    // Load prescriptions on first composition
    LaunchedEffect(Unit) {
        viewModel.loadPrescriptions()
    }

    Scaffold(
        topBar = {
            MedTimeTopAppBar(
                showLogout = true,
                onLogoutClick = {
                    UserSession.clearUser()
                    navController?.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = {
            navController?.let {
                com.example.medtime.components.Navbar(navController = it)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Prescriptions",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { viewModel.loadPrescriptions() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }

            // Content based on state
            when (val state = viewModel.prescriptionsState) {
                is PrescriptionListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading prescriptions...")
                        }
                    }
                }

                is PrescriptionListState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadPrescriptions() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is PrescriptionListState.Success -> {
                    if (state.prescriptions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = "No Prescriptions Yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Start by analyzing your first prescription",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(
                                items = state.prescriptions,
                                key = { it.id }
                            ) { prescription ->
                                ExpandablePrescriptionCard(
                                    prescription = prescription,
                                    onDelete = { viewModel.deletePrescription(prescription.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}