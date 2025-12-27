package com.example.medtime.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medtime.data.ParsedMedication
import com.example.medtime.ui.theme.*


private val LightPurple = Color(0xFF8E44AD)
@Composable
fun NotificationPreviewCard(
    medications: List<ParsedMedication>,
    notificationType: String,
    modifier: Modifier = Modifier
) {
    if (medications.isEmpty()) return

    // Calculate total notifications
    val totalNotifications = medications.sumOf { medication ->
        val duration = medication.durationDays ?: 0
        medication.times.size * duration
    }


    val notificationIcon = remember(notificationType) {
        when (notificationType.lowercase()) {
            "alarm" -> Icons.Default.NotificationsActive
            else -> Icons.Default.Notifications
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Elegant header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerGradient)
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon with subtle background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = notificationIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Notification Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = notificationType.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Medications list
                medications.forEachIndexed { index, medication ->
                    val duration = medication.durationDays ?: 0
                    val notificationCount = medication.times.size * duration

                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Individual medication card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Blue600.copy(alpha = 0.3f),
                                        LightPurple.copy(alpha = 0.3f)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Blue600.copy(alpha = 0.05f),
                                        LightPurple.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            // Medication name with accent
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            brush = accentGradient,
                                            shape = MaterialTheme.shapes.small
                                        )
                                )
                                Text(
                                    text = medication.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkBlue
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Times with icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = LightPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = medication.times.joinToString(", "),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Duration info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Duration: $duration days",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                // Notification count badge
                                Box(
                                    modifier = Modifier
                                        .background(
                                            brush = accentGradient,
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$notificationCount notifications",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Elegant divider
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Blue600.copy(alpha = 0.3f),
                                    LightPurple.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Total summary with elegant styling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    DarkPurple.copy(alpha = 0.1f),
                                    Blue600.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(DarkPurple, Blue600)
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Reminders",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = DarkPurple
                            )
                            Text(
                                text = "Scheduled for this prescription",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        // Prominent total count
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = headerGradient,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "$totalNotifications",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}