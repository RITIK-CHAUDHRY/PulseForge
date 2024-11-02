
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.data.model.Exercise
import com.pulseforge.data.model.PerformanceRecord
import com.pulseforge.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import java.util.*

@Composable
fun ActiveWorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    workoutRoutine: WorkoutRoutine,
    onNavigateBack: () -> Unit,
    onWorkoutComplete: () -> Unit
) {
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var isWorkoutComplete by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    val performanceRecords = remember { mutableStateListOf<PerformanceRecord>() }

    LaunchedEffect(currentExerciseIndex, isPaused) {
        if (currentExerciseIndex < workoutRoutine.exercises.size && !isPaused) {
            val exercise = workoutRoutine.exercises[currentExerciseIndex]
            remainingTime = exercise.duration ?: 60 // Default to 60 seconds if duration is not set
            while (remainingTime > 0) {
                delay(1000)
                remainingTime--
            }
        } else if (currentExerciseIndex >= workoutRoutine.exercises.size) {
            isWorkoutComplete = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workoutRoutine.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (!isWorkoutComplete) {
                val currentExercise = workoutRoutine.exercises[currentExerciseIndex]
                ExerciseCard(
                    exercise = currentExercise,
                    remainingTime = remainingTime,
                    isPaused = isPaused,
                    onPauseResume = { isPaused = !isPaused },
                    onComplete = {
                        performanceRecords.add(
                            PerformanceRecord(
                                date = Date(),
                                exerciseName = currentExercise.name,
                                completedSets = currentExercise.sets,
                                completedReps = currentExercise.reps,
                                weight = currentExercise.weight,
                                duration = currentExercise.duration,
                                distance = currentExercise.distance
                            )
                        )
                        currentExerciseIndex++
                        isPaused = false
                    }
                )
            } else {
                WorkoutSummary(performanceRecords)
                Button(
                    onClick = {
                        workoutViewModel.saveWorkoutPerformance(workoutRoutine.id, performanceRecords)
                        onWorkoutComplete()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Finish Workout")
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    remainingTime: Int,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = exercise.name, style = MaterialTheme.typography.h6)
            Text(text = "Sets: ${exercise.sets}, Reps: ${exercise.reps}", style = MaterialTheme.typography.body1)
            exercise.weight?.let { Text(text = "Weight: $it kg", style = MaterialTheme.typography.body1) }
            Text(text = "Time Remaining: $remainingTime seconds", style = MaterialTheme.typography.body1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onPauseResume) {
                    Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, 
                         contentDescription = if (isPaused) "Resume" else "Pause")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPaused) "Resume" else "Pause")
                }
                Button(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Complete")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Complete")
                }
            }
        }
    }
}

@Composable
fun WorkoutSummary(performanceRecords: List<PerformanceRecord>) {
    Text("Workout Summary", style = MaterialTheme.typography.h6)
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn {
        items(performanceRecords) { record ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                elevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = record.exerciseName, style = MaterialTheme.typography.subtitle1)
                    Text(text = "Sets: ${record.completedSets}, Reps: ${record.completedReps}", style = MaterialTheme.typography.body2)
                    record.weight?.let { Text(text = "Weight: $it kg", style = MaterialTheme.typography.body2) }
                    record.duration?.let { Text(text = "Duration: $it seconds", style = MaterialTheme.typography.body2) }
                    record.distance?.let { Text(text = "Distance: $it km", style = MaterialTheme.typography.body2) }
                }
            }
        }
    }
}
