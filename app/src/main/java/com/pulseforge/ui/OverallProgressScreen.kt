
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulseforge.viewmodel.WorkoutViewModel
import com.pulseforge.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OverallProgressScreen(
    workoutViewModel: WorkoutViewModel,
    authViewModel: AuthViewModel
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val workoutRoutines by workoutViewModel.workoutRoutines.collectAsState()

    val totalWorkouts = workoutRoutines.size
    val completedWorkouts = workoutRoutines.count { it.scheduledDate != null && it.scheduledDate.before(Date()) }
    val totalExercises = workoutRoutines.sumOf { it.exercises.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overall Progress") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            userProfile?.let { profile ->
                Text("Welcome, ${profile.displayName}", style = MaterialTheme.typography.h5)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Fitness Goal: ${profile.fitnessGoal}", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(24.dp))
            }

            ProgressCard(
                title = "Total Workouts",
                value = totalWorkouts.toString()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProgressCard(
                title = "Completed Workouts",
                value = completedWorkouts.toString()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProgressCard(
                title = "Total Exercises",
                value = totalExercises.toString()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // TODO: Add more progress metrics and charts here
        }
    }
}

@Composable
fun ProgressCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.subtitle1)
            Text(text = value, style = MaterialTheme.typography.h6)
        }
    }
}
