
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.WorkoutRoutine
import com.pulseforge.viewmodel.WorkoutViewModel

@Composable
fun RecommendedWorkoutsScreen(
    workoutViewModel: WorkoutViewModel,
    onWorkoutSelected: (WorkoutRoutine) -> Unit
) {
    val recommendedWorkouts by workoutViewModel.recommendedWorkouts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recommended Workouts",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (recommendedWorkouts.isEmpty()) {
            Text(
                text = "No recommendations available. Complete more workouts to get personalized recommendations!",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn {
                items(recommendedWorkouts) { workout ->
                    RecommendedWorkoutItem(
                        workout = workout,
                        onWorkoutSelected = onWorkoutSelected
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendedWorkoutItem(
    workout: WorkoutRoutine,
    onWorkoutSelected: (WorkoutRoutine) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = workout.name,
                style = MaterialTheme.typography.h6
            )
            Text(
                text = workout.description,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = "Exercises: ${workout.exercises.size}",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(top = 4.dp)
            )
            Button(
                onClick = { onWorkoutSelected(workout) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 8.dp)
            ) {
                Text("View Workout")
            }
        }
    }
}
