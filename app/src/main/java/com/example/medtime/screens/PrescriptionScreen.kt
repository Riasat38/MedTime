package com.example.medtime.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: PrescriptionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Snackbar host state
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(viewModel.saveState) {
        if (viewModel.saveState is SaveState.Success) {
            snackbarHostState.showSnackbar(
                message = "Prescription saved successfully!",
                duration = SnackbarDuration.Short
            )
            // Reset the screen after showing snackbar
            capturedBitmap = null
            viewModel.resetAnalysis()
        }
    }

    // Helper function to prepare bitmap for API (convert to software bitmap)
    fun prepareBitmapForAnalysis(bitmap: Bitmap): Bitmap {
        return try {
            // Convert hardware bitmap to software bitmap if needed
            if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false) ?: bitmap
            } else {
                bitmap
            }
        } catch (e: Exception) {
            Log.e("PrescriptionScreen", "Error preparing bitmap: ${e.message}", e)
            bitmap // Return original if conversion fails
        } catch (e: OutOfMemoryError) {
            Log.e("PrescriptionScreen", "Out of memory preparing bitmap", e)
            bitmap // Return original if OOM
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, imageUri!!)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = false
                        // Set sample size to reduce memory usage
                        decoder.setTargetSampleSize(2)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }
                val preparedBitmap = prepareBitmapForAnalysis(bitmap)
                capturedBitmap = preparedBitmap
                viewModel.analyzePrescription(preparedBitmap)
            } catch (e: OutOfMemoryError) {
                Log.e("PrescriptionScreen", "Out of memory processing camera image", e)
            } catch (e: Exception) {
                Log.e("PrescriptionScreen", "Error processing camera image: ${e.message}", e)
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = false
                        // Set sample size to reduce memory usage
                        decoder.setTargetSampleSize(2)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                val preparedBitmap = prepareBitmapForAnalysis(bitmap)
                capturedBitmap = preparedBitmap
                viewModel.analyzePrescription(preparedBitmap)
            } catch (e: OutOfMemoryError) {
                Log.e("PrescriptionScreen", "Out of memory processing gallery image", e)
            } catch (e: Exception) {
                Log.e("PrescriptionScreen", "Error processing gallery image: ${e.message}", e)
            }
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

            Spacer(modifier = Modifier.height(24.dp))
            // Display captured image
            capturedBitmap?.let { bitmap ->
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
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Selected prescription",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
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
                            // Save button
                            GradientButton(
                                text = "Save",
                                onClick = { viewModel.savePrescription(context) },
                                modifier = Modifier.fillMaxWidth(),
                                isOutlined = false,
                                icon = Icons.Default.Save,
                                height = 50.dp
                            )
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
                        is SaveState.Success -> {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    text = "✓ Prescription saved successfully!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(16.dp)
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
                                        onClick = { viewModel.savePrescription(context) },
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
                            capturedBitmap = null
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
                            capturedBitmap = null
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

