
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.data.model.ExerciseType
import com.pulseforge.viewmodel.WorkoutViewModel

@Composable
fun RecommendedWorkoutDetailScreen(
    workoutViewModel: WorkoutViewModel,
    workoutId: Int,
    onNavigateBack: () -> Unit,
    onStartWorkout: (WorkoutRoutine) -> Unit
) {
    val workout = remember(workoutId) {
        workoutViewModel.getWorkoutRoutineById(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(workout?.name ?: "Workout Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        workout?.let { routine ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = routine.description,
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Exercises:",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(routine.exercises) { exercise ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
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
                        }
                    }
                }
                Button(
                    onClick = { onStartWorkout(routine) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Start Workout")
                }
            }
        } ?: run {
            Text("Workout not found")
        }
    }
}
