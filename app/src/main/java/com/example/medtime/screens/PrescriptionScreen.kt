package com.example.medtime.screens


import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.medtime.components.GradientButton
import com.example.medtime.components.MedTimeTopAppBar
import com.example.medtime.components.MedicationCard
import com.example.medtime.data.UserSession
import com.example.medtime.viewmodel.AnalysisState
import com.example.medtime.viewmodel.PrescriptionViewModel
import com.example.medtime.viewmodel.SaveState
import java.io.File
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.medtime.components.NotificationPreviewCard
import com.example.medtime.ui.theme.Blue600


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: PrescriptionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var selectedNotification by remember { mutableStateOf("") }
    var expandedNotification by remember { mutableStateOf(false) }
    val notificationOptions = listOf("push", "alarm") //types of notification
    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(viewModel.saveState) {
        if (viewModel.saveState is SaveState.Success) {
            snackbarHostState.showSnackbar(
                message = "Prescription saved successfully!",
                duration = SnackbarDuration.Short
            )
            // Reset the screen after showing snackbar
            kotlinx.coroutines.delay(500)
            capturedImageUri = null
            selectedNotification = ""
            viewModel.resetAnalysis()

        }
    }
    //Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("PrescriptionScreen", "Notification permission granted")
        } else {
            // Show a message to user
            Toast.makeText(context, "Notification permission is required for reminders", Toast.LENGTH_LONG).show()
        }
    }

    // Request permission on first launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            capturedImageUri = imageUri
            viewModel.setSelectedImage(imageUri!!)
            viewModel.analyzeImage(context)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            capturedImageUri = it
            viewModel.setSelectedImage(it)
            viewModel.analyzeImage(context)
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Create temp file for camera
            val photoFile = File.createTempFile(
                "prescription_",
                ".jpg",
                context.cacheDir
            ).apply {
                createNewFile()
                deleteOnExit()
            }

            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            cameraLauncher.launch(imageUri)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
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
                com.example.medtime.components.Navbar(
                    navController = it
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Prescription Analysis",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Choose how to upload your prescription:",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Camera and Gallery Buttons Side by Side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Camera Button
                GradientButton(
                    text = "Camera",
                    onClick = {
                        permissionLauncher.launch(android.Manifest.permission.CAMERA)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.analysisState !is AnalysisState.Loading,
                    isOutlined = false,
                    icon = Icons.Default.Camera,
                    height = 50.dp
                )

                // Gallery Button
                GradientButton(
                    text = "Gallery",
                    onClick = {
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = viewModel.analysisState !is AnalysisState.Loading,
                    isOutlined = false,
                    icon = Icons.Default.Photo,
                    height = 50.dp
                )
            }


            Spacer(modifier = Modifier.height(24.dp))
            // Display captured image
            capturedImageUri?.let { uri ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selected Image:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(uri)
                                .crossfade(true)
                                .size(800)
                                .build(),
                            contentDescription = "Selected prescription",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Display analysis state
            when (val state = viewModel.analysisState) {
                is AnalysisState.Idle -> {
                    Text(text="Using Models from Google Gemini")
                }
                is AnalysisState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Analyzing prescription...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is AnalysisState.Success -> {
                    //On success
                    Text(
                        text = "Analysis Complete ✓",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Review and edit the extracted medications below:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Prescription Title") },
                        placeholder = { Text("Enter a title for the prescription") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue600,
                            focusedLabelColor = Blue600,
                            cursorColor = Blue600
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Prescription title",
                                tint = Blue600
                            )
                        }
                    )

                    // Display each medication -> editable field
                    state.medications.forEachIndexed { index, medication ->
                        MedicationCard(
                            medication = medication,
                            index = index,
                            timeEdit = true,
                            onMedicationUpdated = { updatedMedication ->
                                viewModel.updateMedication(index, updatedMedication)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display which model was used
                    Text(
                        text = "Analyzed using: ${state.modelUsed}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save state handling
                    when (val saveState = viewModel.saveState) {
                        is SaveState.Idle -> {

                            ExposedDropdownMenuBox(
                                expanded = expandedNotification,
                                onExpandedChange = { expandedNotification = !expandedNotification },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedNotification,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Reminder Method") },
                                    placeholder = { Text("Select how you want to be reminded") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedNotification)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                )
                                ExposedDropdownMenu(
                                    expanded = expandedNotification,
                                    onDismissRequest = { expandedNotification = false }
                                ) {
                                    notificationOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                selectedNotification = option
                                                expandedNotification = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val canSave = selectedNotification.isNotEmpty() &&
                                    viewModel.title.isNotBlank() &&
                                    state.medications.isNotEmpty()
                            //validation
                            if (!canSave) {
                                Text(
                                    text = when {
                                        selectedNotification.isEmpty() -> "Please select a reminder method"
                                        viewModel.title.isBlank() -> "Please enter a prescription title"
                                        else -> "Please add at least one medication"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }

                            if (selectedNotification.isNotEmpty()){
                                // Show notification preview if notification type is selected
                                NotificationPreviewCard(
                                    medications = state.medications,
                                    notificationType = selectedNotification,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))


                            // Save button
                            GradientButton(
                                text = "Save",
                                onClick = { viewModel.savePrescription(context, selectedNotification) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canSave,
                                isOutlined = false,
                                icon = Icons.Default.Save,
                                height = 50.dp
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                        }
                        is SaveState.Success -> {

                      }
                        is SaveState.Saving -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Saving prescription...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        is SaveState.Error -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Failed to save: ${saveState.message}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    GradientButton(
                                        text = "Retry Save",
                                        onClick = { viewModel.savePrescription(context, selectedNotification) },
                                        modifier = Modifier.fillMaxWidth(),
                                        isOutlined = false,
                                        icon = Icons.Default.Save,
                                        height = 44.dp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reset button
                    GradientButton(
                        text = "Analyze Another Prescription",
                        onClick = {
                            capturedImageUri = null
                            viewModel.resetAnalysis()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isOutlined = true,
                        height = 50.dp
                    )
                }
                is AnalysisState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Retry button
                    GradientButton(
                        text = "Try Again",
                        onClick = {
                            capturedImageUri = null
                            viewModel.resetAnalysis()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isOutlined = false,
                        height = 50.dp
                    )
                }
            }
        }
    }
}

