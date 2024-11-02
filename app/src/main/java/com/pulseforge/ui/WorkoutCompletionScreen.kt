
package com.pulseforge.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pulseforge.data.model.PerformanceRecord
import com.pulseforge.viewmodel.WorkoutViewModel
import com.pulseforge.viewmodel.AuthViewModel

@Composable
fun WorkoutCompletionScreen(
    workoutViewModel: WorkoutViewModel,
    authViewModel: AuthViewModel,
    performanceRecords: List<PerformanceRecord>,
    onNavigateToHome: () -> Unit
) {
    val userProfile by authViewModel.userProfile.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.updateStreak()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Completed") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Celebration,
                contentDescription = "Celebration",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Congratulations!",
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "You've completed your workout. Keep up the great work!",
                style = MaterialTheme.typography.body1,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            userProfile?.let { profile ->
                Text(
                    "Current Streak: ${profile.streakCount} days",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Workout Summary",
                style = MaterialTheme.typography.h6
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
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
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNavigateToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Return to Home")
            }
        }
    }
}
