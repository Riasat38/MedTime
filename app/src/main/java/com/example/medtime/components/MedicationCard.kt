package com.example.medtime.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.medtime.data.ParsedMedication
import com.example.medtime.ui.theme.Blue600
import com.example.medtime.ui.theme.DarkBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationCard(
    medication: ParsedMedication,
    index: Int,
    timeEdit: Boolean = true,
    onMedicationUpdated: (ParsedMedication) -> Unit,
    modifier: Modifier = Modifier
) {
    var editedMedication by remember { mutableStateOf(medication) }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTimeIndex by remember { mutableStateOf(-1) } // -1 means adding new time

    // Calculate initial hour and minute based on editing state
    val initialHour = remember(editingTimeIndex, editedMedication.times) {
        if (editingTimeIndex >= 0 && editingTimeIndex < editedMedication.times.size) {
            editedMedication.times[editingTimeIndex].split(":").getOrNull(0)?.toIntOrNull() ?: 9
        } else {
            9
        }
    }

    val initialMinute = remember(editingTimeIndex, editedMedication.times) {
        if (editingTimeIndex >= 0 && editingTimeIndex < editedMedication.times.size) {
            editedMedication.times[editingTimeIndex].split(":").getOrNull(1)?.toIntOrNull() ?: 0
        } else {
            0
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Blue600, DarkBlue)
    )

    // Time Picker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = if (editingTimeIndex == -1) "Add Time" else "Edit Time",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour.toString().padStart(2, '0')
                        val minute = timePickerState.minute.toString().padStart(2, '0')
                        val newTime = "$hour:$minute"

                        val updatedTimes = editedMedication.times.toMutableList()
                        if (editingTimeIndex == -1) {
                            // Adding new time
                            updatedTimes.add(newTime)
                        } else {
                            // Editing existing time
                            updatedTimes[editingTimeIndex] = newTime
                        }
                        updatedTimes.sort() // Sort times chronologically

                        editedMedication = editedMedication.copy(times = updatedTimes)
                        onMedicationUpdated(editedMedication)
                        showTimePicker = false
                    }
                ) {
                    Text("Confirm", color = Blue600)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .border(
                width = 2.dp,
                brush = gradientBrush,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header with medication number
            Text(
                text = "Medication ${index + 1}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Blue600
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Name Field
            ReadOnlyField(
                label = "Medicine Name",
                value = editedMedication.name
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dosage Field
            ReadOnlyField(
                label = "Dosage",
                value = editedMedication.dosage
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Frequency Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    ReadOnlyField(
                        label = "Frequency",
                        value = editedMedication.frequency.toString()
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    ReadOnlyField(
                        label = "Type",
                        value = editedMedication.frequencyType
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration Field
            OutlinedTextField(
                value = editedMedication.durationDays?.toString() ?: "",
                onValueChange = { newValue ->
                    // Allow only digits and ensure it's not overly long
                    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
                        val duration = newValue.toIntOrNull()
                        editedMedication = editedMedication.copy(durationDays = duration)
                        onMedicationUpdated(editedMedication)
                    }
                },
                label = { Text("Duration (days)") },
                placeholder = { Text("e.g., 7") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Times Field (Editable with Time Picker)
            EditableTimesField(
                label = "Reminder Times",
                times = editedMedication.times,
                edit = timeEdit,
                onTimeClick = { timeIndex ->
                    editingTimeIndex = timeIndex
                    showTimePicker = true
                },
                onAddTime = {
                    editingTimeIndex = -1
                    showTimePicker = true
                },
                onRemoveTime = { timeIndex ->
                    val updatedTimes = editedMedication.times.toMutableList()
                    updatedTimes.removeAt(timeIndex)
                    editedMedication = editedMedication.copy(times = updatedTimes)
                    onMedicationUpdated(editedMedication)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Instructions Field (Read-only)
            ReadOnlyField(
                label = "Instructions",
                value = editedMedication.instructions
            )
        }
    }
}

@Composable
private fun ReadOnlyField(
    label: String,
    value: String?
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = DarkBlue
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value?.ifEmpty { "Not specified" }?: "Not specified",
            style = MaterialTheme.typography.bodyLarge,
            color = if (value.isNullOrEmpty()) Color.Gray else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun EditableTimesField(
    label: String,
    times: List<String>,
    edit: Boolean = true,
    onTimeClick: (Int) -> Unit,
    onAddTime: () -> Unit,
    onRemoveTime: (Int) -> Unit

) {

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = DarkBlue
            )

            if (edit){IconButton(
                onClick = onAddTime,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add time",
                    tint = Blue600
                )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (times.isEmpty()) {
            Text(
                text = "No times set - tap + to add",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        } else {
            // Display time chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                times.forEachIndexed { index, time ->
                    TimeChip(
                        time = time,
                        onClick = { onTimeClick(index) },
                        onRemove = { onRemoveTime(index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeChip(
    time: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Blue600, DarkBlue)
    )

    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                brush = gradientBrush,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = Blue600,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = DarkBlue
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove time",
                tint = Color.Gray,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}

