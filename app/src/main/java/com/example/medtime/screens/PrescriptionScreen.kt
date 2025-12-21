package com.example.medtime.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Photo
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
import com.example.medtime.viewmodel.AnalysisState
import com.example.medtime.viewmodel.PrescriptionViewModel
import java.io.File
import java.io.IOException

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

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageUri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri!!))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }
                capturedBitmap = bitmap
                viewModel.analyzePrescription(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
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
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                capturedBitmap = bitmap
                viewModel.analyzePrescription(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
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
        topBar = {
            MedTimeTopAppBar()
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
                    isOutlined = true,
                    icon = Icons.Default.Photo,
                    height = 50.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(text="Your photo is being analyzed using  gemini-2.5-flash model by Google")
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
                    // Show nothing or instructions
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Analysis Result:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = state.result,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
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
