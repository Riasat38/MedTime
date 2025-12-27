package com.example.medtime.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medtime.data.ParsedMedication
import com.example.medtime.repository.SavedPrescription
import com.example.medtime.ui.theme.Blue600
import com.example.medtime.ui.theme.DarkBlue
import com.example.medtime.ui.theme.DarkPurple
import com.example.medtime.utils.toParsedMedication

import java.text.SimpleDateFormat
import java.util.*

// Your color scheme

@Composable
fun ExpandablePrescriptionCard(
    prescription: SavedPrescription,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Convert medication maps to ParsedMedication objects
    val medications = remember(prescription.medications) {
        prescription.medications.mapNotNull { it.toParsedMedication() }
    }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(prescription.createdAt) {
        dateFormat.format(prescription.createdAt.toDate())
    }

    // Gradient brush for accents
    val gradientBrushBlue = remember {
        Brush.horizontalGradient(
            colors = listOf(Blue600, DarkBlue)
        )
    }


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {

            Box {
                // Gradient top accent bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(gradientBrushBlue)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(top = 4.dp) // Space for gradient bar
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prescription.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Blue600 // Use your blue for title
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Created: $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Small colored dot indicator
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = Blue600,
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(
                                text = "${medications.size} medication(s)",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkPurple,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete prescription",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)

                            )
                        }

                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Blue600, // Use your blue for expand icon
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Gradient divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(gradientBrushBlue)
                            .padding(bottom = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    NotificationPreviewCard(
                        medications = medications,
                        notificationType = prescription.notificationType,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                }
            }
        }
    }

    // Delete confirmation dialog with your color scheme
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Prescription",
                    color = DarkBlue,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to delete '${prescription.title}'? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Blue600
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }
}

