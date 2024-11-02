
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.Exercise
import com.pulseforge.data.model.ExerciseType
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.viewmodel.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateEditWorkoutRoutineScreen(
    workoutViewModel: WorkoutViewModel,
    routineToEdit: WorkoutRoutine? = null,
    onSave: () -> Unit
) {
    var name by remember { mutableStateOf(routineToEdit?.name ?: "") }
    var description by remember { mutableStateOf(routineToEdit?.description ?: "") }
    var exercises by remember { mutableStateOf(routineToEdit?.exercises ?: emptyList()) }
    var scheduledDate by remember { mutableStateOf(routineToEdit?.scheduledDate) }
    var notificationTime by remember { mutableStateOf(routineToEdit?.notificationTime) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Routine Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Exercises", style = MaterialTheme.typography.h6)
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(exercises) { exercise ->
                ExerciseItem(
                    exercise = exercise,
                    onDelete = {
                        exercises = exercises.filter { it != exercise }
                    },
                    onEdit = { updatedExercise ->
                        exercises = exercises.map { if (it == exercise) updatedExercise else it }
                    }
                )
            }
        }
        Button(
            onClick = { exercises = exercises + Exercise("New Exercise", ExerciseType.STRENGTH, 3, 10) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            Text("Add Exercise")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Schedule: ", style = MaterialTheme.typography.body1)
            if (scheduledDate != null) {
                Text(dateFormatter.format(scheduledDate!!), style = MaterialTheme.typography.body1)
                IconButton(onClick = { scheduledDate = null }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Date")
                }
            } else {
                Text("Not scheduled", style = MaterialTheme.typography.body1)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { showDatePicker = true }) {
                Text(if (scheduledDate == null) "Set Date" else "Change Date")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notification: ", style = MaterialTheme.typography.body1)
            if (notificationTime != null) {
                Text(timeFormatter.format(Date(notificationTime!!)), style = MaterialTheme.typography.body1)
                IconButton(onClick = { notificationTime = null }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Time")
                }
            } else {
                Text("Not set", style = MaterialTheme.typography.body1)
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { showTimePicker = true }) {
                Text(if (notificationTime == null) "Set Time" else "Change Time")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val routine = WorkoutRoutine(
                    id = routineToEdit?.id ?: 0,
                    name = name,
                    description = description,
                    exercises = exercises,
                    scheduledDate = scheduledDate,
                    notificationTime = notificationTime
                )
                if (routineToEdit == null) {
                    workoutViewModel.insertWorkoutRoutine(routine)
                } else {
                    workoutViewModel.updateWorkoutRoutine(routine)
                }
                onSave()
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Save")
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { 
                scheduledDate = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onTimeSelected = { hour, minute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }
                notificationTime = calendar.timeInMillis
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    onDelete: () -> Unit,
    onEdit: (Exercise) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(exercise.name) }
    var type by remember { mutableStateOf(exercise.type) }
    var sets by remember { mutableStateOf(exercise.sets.toString()) }
    var reps by remember { mutableStateOf(exercise.reps.toString()) }
    var weight by remember { mutableStateOf(exercise.weight?.toString() ?: "") }
    var duration by remember { mutableStateOf(exercise.duration?.toString() ?: "") }
    var distance by remember { mutableStateOf(exercise.distance?.toString() ?: "") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExerciseTypeDropdown(
                    selectedType = type,
                    onTypeSelected = { type = it }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it },
                        label = { Text("Sets") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it },
                        label = { Text("Reps") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                when (type) {
                    ExerciseType.STRENGTH -> {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    ExerciseType.CARDIO -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedTextField(
                                value = duration,
                                onValueChange = { duration = it },
                                label = { Text("Duration (sec)") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = distance,
                                onValueChange = { distance = it },
                                label = { Text("Distance (km)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    ExerciseType.FLEXIBILITY -> {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (sec)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        isEditing = false
                        onEdit(
                            Exercise(
                                name = name,
                                type = type,
                                sets = sets.toIntOrNull() ?: 0,
                                reps = reps.toIntOrNull() ?: 0,
                                weight = weight.toFloatOrNull(),
                                duration = duration.toIntOrNull(),
                                distance = distance.toFloatOrNull()
                            )
                        )
                    }) {
                        Text("Save")
                    }
                    TextButton(onClick = { isEditing = false }) {
                        Text("Cancel")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = exercise.name, style = MaterialTheme.typography.subtitle1)
                        Text(text = "Type: ${exercise.type}", style = MaterialTheme.typography.body2)
                        Text(text = "Sets: ${exercise.sets}, Reps: ${exercise.reps}", style = MaterialTheme.typography.body2)
                        when (exercise.type) {
                            ExerciseType.STRENGTH -> exercise.weight?.let { Text(text = "Weight: $it kg", style = MaterialTheme.typography.body2) }
                            ExerciseType.CARDIO -> {
                                exercise.duration?.let { Text(text = "Duration: $it seconds", style = MaterialTheme.typography.body2) }
                                exercise.distance?.let { Text(text = "Distance: $it km", style = MaterialTheme.typography.body2) }
                            }
                            ExerciseType.FLEXIBILITY -> exercise.duration?.let { Text(text = "Duration: $it seconds", style = MaterialTheme.typography.body2) }
                        }
                    }
                    IconButton(onClick = { isEditing = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Exercise")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Exercise")
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseTypeDropdown(
    selectedType: ExerciseType,
    onTypeSelected: (ExerciseType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val types = ExerciseType.values()

    Box {
        OutlinedTextField(
            value = selectedType.name,
            onValueChange = {},
            label = { Text("Exercise Type") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            types.forEach { type ->
                DropdownMenuItem(onClick = {
                    onTypeSelected(type)
                    expanded = false
                }) {
                    Text(text = type.name)
                }
            }
        }
    }
}
